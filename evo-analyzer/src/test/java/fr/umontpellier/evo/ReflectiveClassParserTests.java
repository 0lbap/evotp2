package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.StatisticVisitor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectiveClassParserTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "ClassParser.java");
    private static ClassParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = ClassParser.from(rootPath, parserClassPath);
    }

    @Test
    public void assertParsesPackage() throws IOException {
        var parser = ClassParser.from(rootPath, parserClassPath);
        assertEquals(parser.root(), rootPath);
        assertEquals(parser.pkg(), "fr.umontpellier.evo");
    }

    @Test
    public void assertCachesParser() throws IOException {
        var newParser = ClassParser.from(rootPath, parserClassPath);
        assertEquals(parser, newParser);
    }

    @Test
    public void assertStatisticsWork() {
        assertTrue(parser.accept(StatisticVisitor::new)
                .methods()
                .stream()
                .filter(m -> m.name().equalsIgnoreCase("pkg"))
                .map(StatisticVisitor.Result.Method::lineCount)
                .findAny()
                .isPresent());
    }

}
