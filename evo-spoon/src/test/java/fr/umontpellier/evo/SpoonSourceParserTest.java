package fr.umontpellier.evo;

import fr.umontpellier.evo.aggregator.SpoonCouplageAggregator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpoonSourceParserTest {

    private static final Path rootPath = Path.of(".",  "src", "test", "resources", "examples", "abcd");

    @Test
    public void testParsing() throws IOException {
        var parser = SpoonSourceLoader.INSTANCE.load(rootPath.resolve("B.java")).get(0);
        var result  = parser.accept(new SpoonCouplageAggregator());
        assertEquals(Map.of(Set.of("B", "A"), 1, Set.of("B", "C"), 1), result.couplages());
    }

}
