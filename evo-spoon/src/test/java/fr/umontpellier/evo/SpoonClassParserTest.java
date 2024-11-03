package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.SpoonCouplageVisitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class SpoonClassParserTest {

    private static final Path rootPath = Path.of(".",  "src", "test", "resources", "examples", "abcd");

    @Test
    public void testParsing() throws IOException {
        var parser = SpoonClassParser.from(rootPath, rootPath.resolve("B.java"));
        var result  = parser.accept(SpoonCouplageVisitor::new);
    }

}
