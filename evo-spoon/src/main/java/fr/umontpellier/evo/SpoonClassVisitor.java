package fr.umontpellier.evo;

import spoon.reflect.visitor.CtScanner;

public abstract class SpoonClassVisitor<R extends SpoonClassVisitor.Result> extends CtScanner {

    protected final SpoonClassParser caller;

    protected SpoonClassVisitor(SpoonClassParser caller) {
        this.caller = caller;
    }

    /**
     * Retourne le résultat de la visite de nos classes, agréger dans une classe de type {@link R}.
     *
     * @return le résultat, {@link R}
     */
    public abstract R result();

    /**
     * Représente le type de retour de notre visiteur, qui va servir à agréger des données.
     */
    public interface Result {}

}
