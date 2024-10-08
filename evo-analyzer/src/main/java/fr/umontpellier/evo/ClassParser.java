package fr.umontpellier.evo;

import fr.umontpellier.evo.visitor.PackageResolverVisitor;
import lombok.Data;
import lombok.Getter;
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

@Data
public class ClassParser {

    private static final Map<String, ClassParser> cache = new HashMap<>();

    /**
     * Initialise un parser ou le prend depuis le cache.
     * @param projectRoot racine du projet
     * @param sources sources
     * @return un parser
     */
    public static ClassParser from(Path projectRoot, String sources) {
        var parser = new ClassParser(projectRoot, sources);
        if (cache.containsKey(parser.pkg))
            return cache.get(parser.pkg);
        return parser;
    }

    /**
     * Similaire mais lis le fichier
     *
     * @param projectRoot racine du projet
     * @param file chemin du fichier
     * @return un parser
     * @throws IOException erreur de lecture
     */
    public static ClassParser from(Path projectRoot, Path file) throws IOException {
        return from(projectRoot, readString(file));
    }

    @Getter
    private final Path root;
    @Getter
    private final CompilationUnit compilationUnit;
    @Getter
    private final String pkg;

    /**
     * Initialise le parser et lit l'AST
     *
     * @param root racine
     * @param sources sources
     */
    ClassParser(Path root, String sources) {
        this.root = root;
        var parser = ASTParser.newParser(AST.JLS4);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setUnitName("");
        parser.setSource(sources.toCharArray());
        this.compilationUnit = (CompilationUnit) parser.createAST(null);

        var resolver = accept(PackageResolverVisitor::new);
        this.pkg = resolver.pkg() + "." + resolver.name();
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

    /**
     * Accepte un visiteur et retourne son résultat
     * @param visitor le visiteur
     * @return le resultat
     * @param <R> le resultat
     */
    public <R extends ClassVisitor.Result> R accept(ClassVisitor<R> visitor) {
        this.compilationUnit.accept(visitor);
        return visitor.result();
    }

    /**
     * Similaire mais il est possible de donner une fonction qui retourne un visiteur
     * @param visitorSupplier la fonction
     * @return le résultat
     * @param <R> le résultat
     */
    public <R extends ClassVisitor.Result> R accept(Function<ClassParser, ClassVisitor<R>> visitorSupplier) {
        return accept(visitorSupplier.apply(this));
    }
}
