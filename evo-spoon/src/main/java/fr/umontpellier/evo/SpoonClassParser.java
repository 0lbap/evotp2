package fr.umontpellier.evo;

import lombok.Data;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtTypedElement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.Files.readString;

@Data
public class SpoonClassParser {

    private final CtClass<?> clazz;

    /**
     * Initialise un parser ou le prend depuis le cache.
     * @param projectRoot racine du projet
     * @param sources sources
     * @return un parser
     */
    public static SpoonClassParser from(Path projectRoot, String sources) {
        return new SpoonClassParser(projectRoot, sources);
    }

    /**
     * Similaire mais lis le fichier
     *
     * @param projectRoot racine du projet
     * @param file chemin du fichier
     * @return un parser
     * @throws IOException erreur de lecture
     */
    public static SpoonClassParser from(Path projectRoot, Path file) throws IOException {
        return from(projectRoot, readString(file));
    }

    SpoonClassParser(Path projectRoot, String code) {
        this.clazz = Launcher.parseClass(code);
    }


    /**
     * Accepte un visiteur et retourne son résultat
     * @param visitor le visiteur
     * @return le resultat
     * @param <R> le resultat
     */
    public <R extends SpoonClassVisitor.Result> R accept(SpoonClassVisitor<R> visitor) {
        this.clazz.accept(visitor);
        return visitor.result();
    }

    /**
     * Similaire mais il est possible de donner une fonction qui retourne un visiteur
     * @param visitorSupplier la fonction
     * @return le résultat
     * @param <R> le résultat
     */
    public <R extends SpoonClassVisitor.Result> R accept(Function<SpoonClassParser, SpoonClassVisitor<R>> visitorSupplier) {
        return accept(visitorSupplier.apply(this));
    }

}
