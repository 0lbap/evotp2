package fr.umontpellier.evo;

import lombok.Data;
import lombok.Getter;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.readString;

@Data
public class EclipseSourceParser implements SourceParser {

    @Getter
    private final Path file;
    @Getter
    private final CompilationUnit compilationUnit;

    EclipseSourceParser(Path file) throws IOException {
        this.file = file;
        var parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(readString(file).toCharArray());
        this.compilationUnit = (CompilationUnit) parser.createAST(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends SourceAggregator.Result> T accept(SourceAggregator<T> aggregator) {
        if (!(aggregator instanceof ASTVisitor))
            throw new IllegalArgumentException("L'agrégateur doit être de type ASTVisitor");

        this.compilationUnit.accept((ASTVisitor) aggregator);
        return aggregator.result();
    }
}
