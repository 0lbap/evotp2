package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.CouplageVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CouplageTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "ClassParser.java");
    private static ClassParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = ClassParser.from(rootPath, parserClassPath);
    }

    @Test
    public void generateGraph() {
        assertFalse(parser.accept(CouplageVisitor::new).couplages().isEmpty());
    }

}
