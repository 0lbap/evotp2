package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.PackageResolverVisitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.nio.file.Files.readString;

public class ClassParser {

    private static final Map<String, ClassParser> cache = new HashMap<>();

    public static ClassParser from(Path projectRoot, String sources) {
        var parser = new ClassParser(projectRoot, sources);
        if (cache.containsKey(parser.pkg))
            return cache.get(parser.pkg);
        return parser;
    }

    public static ClassParser from(Path projectRoot, Path file) throws IOException {
        return from(projectRoot, readString(file));
    }

    private final Path root;
    private final CompilationUnit unit;

    private final String pkg;

    ClassParser(Path root, String sources) {
        this.root = root;
        var parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(sources.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        this.unit = (CompilationUnit) parser.createAST(null);

        this.pkg = accept(PackageResolverVisitor::new).pkg();
        if (!cache.containsKey(this.pkg)) {
            cache.put(this.pkg, this);
        }
    }

    public ClassParser resolve(String pkg) throws IOException {
        return new ClassParser(
                root,
                readString(root.resolve(pkg.replace(".", File.pathSeparator)))
        );
    }

    public <R extends ClassVisitor.Result> R accept(ClassVisitor<R> visitor) {
        this.unit.accept(visitor);
        return visitor.result();
    }

    public <R extends ClassVisitor.Result> R accept(Function<ClassParser, ClassVisitor<R>> visitorSupplier) {
        return accept(visitorSupplier.apply(this));
    }

    public Path root() {
        return root;
    }

    public String pkg() {
        return pkg;
    }

    public CompilationUnit compilationUnit() {
        return unit;
    }
}
