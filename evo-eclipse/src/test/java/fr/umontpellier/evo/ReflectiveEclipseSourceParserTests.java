package fr.umontpellier.evo;

import fr.umontpellier.evo.aggregator.PackageAggregator;
import fr.umontpellier.evo.aggregator.StatisticAggregator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectiveEclipseSourceParserTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "EclipseSourceParser.java");
    private static SourceParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = EclipseSourceLoader.INSTANCE.load(parserClassPath).get(0);
    }

    @Test
    public void assertParsesPackage() throws IOException {
        var pkg = parser.accept(PackageAggregator::new);
        assertEquals(pkg.pkg(), "fr.umontpellier.evo");
    }

    @Test
    public void assertStatisticsWork() {
        assertTrue(parser.accept(StatisticAggregator::new)
                .methods()
                .stream()
                .filter(m -> m.name().equalsIgnoreCase("accept"))
                .map(StatisticAggregator.Result.Method::lineCount)
                .findAny()
                .isPresent());
    }

}
