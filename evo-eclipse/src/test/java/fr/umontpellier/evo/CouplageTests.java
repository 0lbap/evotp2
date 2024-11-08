package fr.umontpellier.evo;

import fr.umontpellier.evo.aggregator.CouplageAggregator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CouplageTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "EclipseSourceParser.java");
    private static EclipseSourceParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = (EclipseSourceParser) EclipseSourceLoader.INSTANCE.load(parserClassPath).get(0);
    }

    @Test
    public void generateGraph() {
        assertFalse(parser.accept(CouplageAggregator::new).couplages().isEmpty());
    }

    @Test
    public void testExample() throws IOException {
        var root = Path.of(".", "src", "test", "resources", "examples", "abcd");
        var parser = (EclipseSourceParser) EclipseSourceLoader.INSTANCE.load(root.resolve("A.java")).get(0);
        var couplages = parser.accept(CouplageAggregator::new).couplages();
        System.out.println(couplages);
    }

}
