package fr.umontpellier.evo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Un {@link SourceLoader} est une classe propre à chaque projet permettant l'analyse de classe Java.
 *
 * <p>
 *     Certaines librairies (notamment Eclipse) ont un système de chargement qui est... contestable, le but de cette
 *     classe est de l'abstraire.
 * </p>
 */
public interface SourceLoader {

    /**
     * Charge le dossier/fichier source disponible au chemin indiqué.
     *
     * <p>
     *     Le fonctionnement du chargement des sources est abstrait par l'implémentation de {@link SourceParser},
     *     cependant le comportement des {@link SourceAggregator} que l'on applique eux sont propre à chaque librairie
     *     de parcours de l'AST.
     * </p>
     *
     * @see SourceParser
     * @see SourceAggregator
     *
     * @param path Le chemin vers le dossier/fichier à charger
     * @return Une liste de {@link SourceParser}
     */
    List<SourceParser> load(Path path) throws IOException;

}
