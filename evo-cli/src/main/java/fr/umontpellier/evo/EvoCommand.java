package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.FileTreeAgregationVisitor;
import fr.umontpellier.evo.visitor.StatisticVisitor;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

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

        var parsers = visitor.paths().stream()
                .map(f -> unwrap(() -> ClassParser.from(root, f)))
                .toList();

        System.out.println("Lecture de " + parsers.size() + " classes effectuée avec succès.");
        System.out.println("Nombre de ligne par méthode moyen: " + parsers.stream()
                .map(p -> p.accept(StatisticVisitor::new))
                .flatMap(p -> p.methods().stream())
                .mapToInt(StatisticVisitor.Result.Method::lineCount)
                .average()
                .orElse(0.0));

        return 0;
    }

}
