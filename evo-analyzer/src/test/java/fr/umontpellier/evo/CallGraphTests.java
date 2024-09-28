package fr.umontpellier.evo;


import fr.umontpellier.evo.visitor.CallGraphVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class CallGraphTests {

    private static final Path rootPath = Path.of(".",  "src", "main", "java", "fr", "umontpellier", "evo");
    private static final Path parserClassPath = Path.of(rootPath.toString(), "ClassParser.java");
    private static ClassParser parser;

    @BeforeAll
    public static void initialize() throws IOException {
        parser = ClassParser.from(rootPath, parserClassPath);
    }

    @Test
    public void generateGraph() {
        parser.accept(CallGraphVisitor::new).calls().forEach((method, calls) -> {
            System.out.println(method + " " + calls);
        });
    }

}
