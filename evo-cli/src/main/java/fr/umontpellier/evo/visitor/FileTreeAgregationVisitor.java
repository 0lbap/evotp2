package fr.umontpellier.evo.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileTreeAgregationVisitor implements FileVisitor<Path> {

    private final List<Path> paths = new ArrayList<>();
    private final Optional<String> extension;

    public FileTreeAgregationVisitor(Optional<String> extension) {
        this.extension = extension;
    }

    public FileTreeAgregationVisitor() {
        this.extension = Optional.empty();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (extension.isPresent() && file.getFileName().toString().endsWith(extension.get())) {
            paths.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    public List<Path> paths() {
        return paths;
    }
}
