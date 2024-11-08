package fr.umontpellier.evo;

import fr.umontpellier.evo.aggregator.SpoonCouplageAggregator;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableNode;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.*;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Link.to;

@CommandLine.Command(name = "evo")
public class EvoCommand {

    @CommandLine.Command(name = "couplage", description = "Retourne le graphe de couplage, au format .dot de graphviz")
    public Integer couplage(
            @CommandLine.Parameters(arity = "1", paramLabel = "root") Path root
    ) throws IOException {
        var couplages = SpoonSourceLoader.INSTANCE
                .load(root)
                .stream()
                .map(p -> p.accept(new SpoonCouplageAggregator()).couplages())
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
        var total = couplages.values()
                .stream()
                .mapToInt(i -> i)
                .sum();
        var nodes = couplages.keySet().stream()
                .flatMap(Set::stream)
                .distinct()
                .collect(Collectors.toMap(e -> e, Factory::mutNode));

        Graph g = graph("couplage")
                .graphAttr().with(Color.TRANSPARENT.background())
                .graphAttr().with(Color.WHITE.font())
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"), Color.WHITE.font(), Color.WHITE)
                .linkAttr().with(Color.WHITE.font(), Color.WHITE);
        for (var entry: couplages.entrySet()) {
            var couple = new ArrayList<>(entry.getKey());
            var a = nodes.get(couple.get(0));
            var b = nodes.get(couple.get(1));
            var couplage = entry.getValue();
            var m = couplages.entrySet().stream()
                    .filter(e -> e.getKey().contains(couple.get(0)))
                    .mapToInt(Map.Entry::getValue)
                    .max().orElse(couplage);

            a.addLink(to(b).with(attr("minlen", m - couplage + 1), Label.of(String.format("%.2f", couplage / (double)total))));
        }
        for (var node: nodes.values()) {
            g = g.with(node);
        }

        System.out.println(Graphviz.fromGraph(g).render(Format.DOT));

        return 0;
    }

    private MutableNode createNodeForCluster(ClassClusterizer.Cluster cluster, boolean isInModule, int clusterId) {
        String label = cluster.getClassNames().toString();

        // Changer la couleur et le label si le cluster fait partie d'un module
        if (isInModule) {
            return mutNode("C" + clusterId)
                    .add(Label.of("Module: " + label))
                    .add(Color.RED); // Couleur spécifique pour les modules
        } else {
            return mutNode("C" + clusterId)
                    .add(Label.of(label)) // Label par défaut
                    .add(Color.WHITE);    // Couleur par défaut
        }
    }

    @CommandLine.Command(name = "clusterize", description = "Retourne les clusters des classes, au format .dot de graphviz")
    public Integer clusterize(
            @CommandLine.Option(names = {"--cp", "-cp"}) Integer CP,
            @CommandLine.Parameters(arity = "1", paramLabel = "root") Path root
    ) throws IOException {
        var couplages = SpoonSourceLoader.INSTANCE
                .load(root)
                .stream()
                .map(p -> p.accept(new SpoonCouplageAggregator()).couplages())
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

        var total = couplages.values()
                .stream()
                .mapToInt(i -> i)
                .sum();

        var dendro = ClassClusterizer.clusterize(couplages, total);
        var modules = CP != null ? dendro.getTopModules(CP) : new HashSet<>();

        // Convert the dendrogram into a Graphviz graph
        Graph g = graph("clusterization")
                .graphAttr().with(Color.TRANSPARENT.background())
                .graphAttr().with(Color.WHITE.font())
                .graphAttr().with(Rank.dir(BOTTOM_TO_TOP))
                .nodeAttr().with(Font.name("arial"), Color.WHITE.font(), Color.WHITE, Shape.RECTANGLE)
                .linkAttr().with(Color.WHITE.font(), Color.WHITE);

        Map<ClassClusterizer.Cluster, MutableNode> nodes = new HashMap<>();
        int clusterId = 1;

        // Create nodes for each individual class and cluster
        for (ClassClusterizer.Dendrogram.Step step : dendro.getSteps()) {
            ClassClusterizer.Cluster cluster1 = step.cluster1;
            ClassClusterizer.Cluster cluster2 = step.cluster2;
            ClassClusterizer.Cluster mergedCluster = step.mergedCluster;
            int weight = step.weight;

            // Créer les noeuds pour cluster1, cluster2 et mergedCluster
            nodes.putIfAbsent(cluster1, createNodeForCluster(cluster1, modules.contains(cluster1), clusterId++));
            nodes.putIfAbsent(cluster2, createNodeForCluster(cluster2, modules.contains(cluster2), clusterId++));
            nodes.putIfAbsent(mergedCluster, createNodeForCluster(mergedCluster, modules.contains(mergedCluster), clusterId++));

            // Ajouter une arête entre les clusters fusionnés
            g = g.with(
                    nodes.get(cluster1).addLink(
                            to(nodes.get(mergedCluster))
                                    .with(Label.of(String.format("%.2f", weight / (double) total)), attr("minlen", 1))
                    ),
                    nodes.get(cluster2).addLink(
                            to(nodes.get(mergedCluster))
                                    .with(Label.of(String.format("%.2f", weight / (double) total)), attr("minlen", 1))
                    )
            );
        }

        // Add all nodes to the graph
        for (var node : nodes.values()) {
            g = g.with(node);
        }

        // Output the Graphviz representation
        System.out.println(Graphviz.fromGraph(g).render(Format.DOT));

        return 0;
    }



}
