package fr.umontpellier.evo;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class ClassVisitor<R extends ClassVisitor.Result> extends ASTVisitor {

    protected final ClassParser caller;

    protected ClassVisitor(ClassParser caller) {
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
