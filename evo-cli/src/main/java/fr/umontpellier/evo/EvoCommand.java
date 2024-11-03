package fr.umontpellier.evo;

import fr.umontpellier.evo.utils.Colors;
import fr.umontpellier.evo.visitor.CallGraphVisitor;
import fr.umontpellier.evo.visitor.CouplageVisitor;
import fr.umontpellier.evo.visitor.FileTreeAgregationVisitor;
import fr.umontpellier.evo.visitor.StatisticVisitor;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.model.Node;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static fr.umontpellier.evo.utils.Streams.unwrap;
import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.*;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Link.to;
import static java.nio.file.Files.walkFileTree;

@CommandLine.Command(name = "evo")
public class EvoCommand {

    @CommandLine.Command(name = "analyze", description = "Analyse l'ensemble des classes dans un répertoire donnée et renvoie les statistiques demandées.")
    public Integer analyze(
            @CommandLine.Parameters(arity = "1", paramLabel = "root", description = "Racine des dossiers contenant les classes Java")  Path root,
            @CommandLine.Option(names = {"method_size", "M"}, description = "Précise le nombre X de méthode") long methodSize
    ) throws IOException {
        var visitor = new FileTreeAgregationVisitor(Optional.of(".java"));
        // Atroce
        walkFileTree(root, visitor);

        var stats = visitor.paths().stream()
                .map(f -> unwrap(() -> ClassParser.from(root, f)))
                .filter(Objects::nonNull)
                .map(p -> p.accept(StatisticVisitor::new))
                .collect(Collectors.toList());

        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre de classes dans l'application (sans compter les sous-classes) : " + Colors.brightBlue + stats.size() + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre de lignes de code de l’application :                            " + Colors.brightBlue + stats.stream()
                .map(StatisticVisitor.Result::clazz)
                .mapToInt(StatisticVisitor.Result.Class::lineCount)
                .sum() + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre total de méthodes de l’application :                            " + Colors.brightBlue + stats.stream()
                .map(StatisticVisitor.Result::methods)
                .mapToInt(List::size)
                .sum() + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre total de package dans l'application :                           " + Colors.brightBlue + stats.stream()
                .map(StatisticVisitor.Result::pkg)
                .collect(Collectors.toSet())
                .size() + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre moyen de méthodes par classe :                                  " + Colors.brightBlue + stats.stream()
                .map(StatisticVisitor.Result::methods)
                .mapToInt(List::size)
                .average()
                .orElse(0.0) + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre moyen d'attribut par classe :                                   " + Colors.brightBlue + stats.stream()
                .map(StatisticVisitor.Result::fields)
                .mapToInt(List::size)
                .average()
                .orElse(0.0)+ Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre de ligne par méthode moyen :                                    " + Colors.brightBlue + String.format("%1.2f", stats.stream()
                .flatMap(p -> p.methods().stream())
                .mapToInt(StatisticVisitor.Result.Method::lineCount)
                .average()
                .orElse(0.0)) + Colors.reset);
        var classSortedByMethods = stats.stream()
                .sorted(Comparator.comparingInt(m -> -m.methods().size()))
                .map(StatisticVisitor.Result::clazz)
                .map(StatisticVisitor.Result.Class::name)
                .collect(Collectors.toList());
        var classSortedByAttributes = stats.stream()
                .sorted(Comparator.comparingInt(m -> -m.fields().size()))
                .map(StatisticVisitor.Result::clazz)
                .map(StatisticVisitor.Result.Class::name)
                .collect(Collectors.toList());
        var tenPercentMethods = classSortedByMethods.stream()
                .limit((long) Math.ceil(classSortedByMethods.size() * 0.1))
                .collect(Collectors.toSet());
        var tenPercentAttributes = classSortedByAttributes.stream()
                .limit((long) Math.ceil(classSortedByAttributes.size() * 0.1))
                .collect(Collectors.toSet());
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " 10% des classes avec le plus de méthodes :                             " + Colors.brightBlue + String.join(", ", tenPercentMethods) + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " 10% des classes avec le plus d'attributs :                             " + Colors.brightBlue + String.join(", ", tenPercentAttributes) + Colors.reset);
        var intersection = new HashSet<>(tenPercentAttributes);
        intersection.retainAll(tenPercentMethods);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Intersection des deux catégories précédantes :                         " + Colors.brightBlue + (intersection.isEmpty() ? "∅" : String.join(", ", intersection)) + Colors.reset);

        var classWithXmethods = stats.stream()
                .filter(c -> c.methods().size() > methodSize)
                .map(StatisticVisitor.Result::clazz)
                .map(StatisticVisitor.Result.Class::name)
                .collect(Collectors.toSet());
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Classes avec plus de " + methodSize + " méthodes :                                      " + Colors.brightBlue + (classWithXmethods.isEmpty() ? "∅" : String.join(", ", classWithXmethods)) + Colors.reset);

        var methodsSortedByLines = stats.stream()
                .flatMap(r -> r.methods().stream())
                .sorted(Comparator.comparingInt(m -> -m.lineCount()))
                .collect(Collectors.toList());
        var tenPercentMethodsLines = methodsSortedByLines.stream()
                .limit((long) Math.ceil(methodsSortedByLines.size() * 0.1))
                .map(StatisticVisitor.Result.Method::name)
                .collect(Collectors.toSet());
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " 10% des méthodes avec le plus de lignes :                              " + Colors.brightBlue + String.join(", ", tenPercentMethodsLines) + Colors.reset);
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre de paramètre pour la méthode avec le plus de paramètres :       " + Colors.brightBlue + stats.stream()
                .flatMap(r -> r.methods().stream())
                .min(Comparator.comparingInt(m -> -m.parameters()))
                .map(m -> m.name() + "(" + m.parameters() + ")")
                .orElse("∅") + Colors.reset);

        return 0;
    }

    @CommandLine.Command(name = "callgraph", description = "Retourne le graphe d'appel de profondeur 1, au format .dot de graphviz")
    public Integer callGraph(
            @CommandLine.Parameters(arity = "1", paramLabel = "root") Path root
    ) throws IOException {
        var visitor = new FileTreeAgregationVisitor(Optional.of(".java"));
        // Atroce
        walkFileTree(root, visitor);

        Graph g = graph("callgraph<").directed()
                .graphAttr().with(Color.TRANSPARENT.background())
                .graphAttr().with(Color.WHITE.font())
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("arial"))
                .linkAttr().with("class", "link-class");
        var map = visitor.paths().stream()
                .map(f -> unwrap(() -> ClassParser.from(root, f)))
                .filter(Objects::nonNull)
                .flatMap(p -> p.accept(CallGraphVisitor::new).calls().entrySet().stream().filter(e -> !e.getKey().isEmpty()))
                .collect(Collectors.toMap(
                        e -> node(e.getKey()),
                        e -> e.getValue().stream().filter(f -> !f.isEmpty()).map(Factory::node).collect(Collectors.toList()),
                        (list1, list2) -> {
                            List<Node> mergedList = new ArrayList<>(list1);
                            mergedList.addAll(list2);
                            return mergedList;
                        }
                ));
        for (var entry: map.entrySet()) {
            var node = entry.getKey().with(Color.WHITE.font()).with(Color.WHITE);
            for (var edge: entry.getValue()) {
                node = node.link(to(edge.with(Color.WHITE.font()).with(Color.WHITE)).with(Color.WHITE));
            }
            g = g.with(node);
        }

        System.out.println(Graphviz.fromGraph(g).height(100).render(Format.DOT));
        return 0;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @CommandLine.Command(name = "couplage", description = "Retourne le graphe de couplage, au format .dot de graphviz")
    public Integer couplage(
            @CommandLine.Parameters(arity = "1", paramLabel = "root") Path root
    ) throws IOException {
        var visitor = new FileTreeAgregationVisitor(Optional.of(".java"));
        // Atroce
        walkFileTree(root, visitor);

        var couplages = visitor.paths().stream()
                .map(f -> unwrap(() -> ClassParser.from(root, f)))
                .filter(Objects::nonNull)
                .map(parser -> parser.accept(CouplageVisitor::new))
                .map(CouplageVisitor.Result::couplages)
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
        var visitor = new FileTreeAgregationVisitor(Optional.of(".java"));

        walkFileTree(root, visitor);

        var couplages = visitor.paths().stream()
                .map(f -> unwrap(() -> ClassParser.from(root, f)))
                .filter(Objects::nonNull)
                .map(parser -> parser.accept(CouplageVisitor::new))
                .map(CouplageVisitor.Result::couplages)
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
