package fr.umontpellier.evo;

import fr.umontpellier.evo.utils.FileTreeAgregationVisitor;
import lombok.Singular;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.walkFileTree;
import static java.util.Optional.of;

public class EclipseSourceLoader implements SourceLoader {

    public static final EclipseSourceLoader INSTANCE = new EclipseSourceLoader();

    @Override
    public List<SourceParser> load(Path path) throws IOException {
        if (isDirectory(path)) {
            var javaVisitor = new FileTreeAgregationVisitor(of(".java"));
            walkFileTree(path, javaVisitor);

            return javaVisitor.paths()
                    .stream()
                    .map((c) -> {
                        try {
                            return new EclipseSourceParser(c);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toUnmodifiableList());
        }
        return List.of(new EclipseSourceParser(path));
    }

}
