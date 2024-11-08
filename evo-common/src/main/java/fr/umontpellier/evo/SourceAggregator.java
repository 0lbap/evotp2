package fr.umontpellier.evo;

/**
 * Un {@link SourceAggregator} permet l'agrégation d'une ou plusieurs données autour du {@link SourceParser}
 * auquel on l'applique.
 *
 * <pre>
 *      SourceLoader loader = ...;
 *      List<String> methods = loader.accept(new FindMethodsAggregator())
 *          .result();
 * </pre>
 *
 * Les agrégateurs sont propres à chaque platforme.
 *
 * @param <T> le type du résultat
 */
public interface SourceAggregator<T extends SourceAggregator.Result> {

    /**
     * Retourne les résultats de l'agrégation, une fois que les sources.
     *
     * <p>
     *     Il faut noter que les résultats ne sont pas disponibles du moment que l'instance de l'agrégateur
     *     n'a pas été appelé par {@link SourceParser#accept(SourceAggregator)}.
     * </p>
     *
     * @return le résultat
     */
    T result();

    interface Result {}

}
