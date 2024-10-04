package fr.umontpellier.evo;

import fr.umontpellier.evo.utils.Colors;
import fr.umontpellier.evo.visitor.CallGraphVisitor;
import fr.umontpellier.evo.visitor.FileTreeAgregationVisitor;
import fr.umontpellier.evo.visitor.StatisticVisitor;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static fr.umontpellier.evo.utils.Streams.unwrap;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;
import static java.nio.file.Files.walkFileTree;

@CommandLine.Command(name = "evo")
public class EvoCommand {

    @CommandLine.Command(name = "analyze")
    public Integer analyze(
            @CommandLine.Parameters(arity = "1", paramLabel = "root")  Path root
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
        System.out.println(Colors.brightGreen + "->" + Colors.reset + " Nombre de ligne par méthode moyen :                                    " + Colors.brightBlue + String.format("%1.2f", stats.stream()
                .flatMap(p -> p.methods().stream())
                .mapToInt(StatisticVisitor.Result.Method::lineCount)
                .average()
                .orElse(0.0)) + Colors.reset);

        return 0;
    }

    @CommandLine.Command(name = "callgraph")
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

}
