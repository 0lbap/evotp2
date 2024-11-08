package fr.umontpellier.evo;


import fr.umontpellier.evo.aggregator.CallGraphAggregator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CallGraphTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "EclipseSourceParser.java");
    private static EclipseSourceParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = (EclipseSourceParser) EclipseSourceLoader.INSTANCE.load(parserClassPath).get(0);
    }

    @Test
    public void generateGraph() {
        parser.accept(CallGraphAggregator::new).calls().forEach((method, calls) -> {
            System.out.println(method + " " + calls);
        });
    }

}
