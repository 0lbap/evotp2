package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.FileTreeAgregationVisitor;
import fr.umontpellier.evo.visitor.StatisticVisitor;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fr.umontpellier.evo.utils.Streams.unwrap;
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
                .toList();

        System.out.println("- Nombre de classes dans l'application (sans compter les sous-classes): " + stats.size());
        System.out.println("- Nombre de lignes de code de l’application: " + stats.stream()
                .map(StatisticVisitor.Result::clazz)
                .mapToInt(StatisticVisitor.Result.Class::lineCount)
                .sum());
        System.out.println("- Nombre total de méthodes de l’application: " + stats.stream()
                .map(StatisticVisitor.Result::methods)
                .mapToInt(List::size)
                .sum());
        System.out.println("- Nombre de ligne par méthode moyen: " + stats.stream()
                .flatMap(p -> p.methods().stream())
                .mapToInt(StatisticVisitor.Result.Method::lineCount)
                .average()
                .orElse(0.0));

        return 0;
    }

}
