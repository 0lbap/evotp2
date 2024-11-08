package fr.umontpellier.evo;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class EclipseSourceAggregator<T extends SourceAggregator.Result> extends ASTVisitor implements SourceAggregator<T> {

    protected final SourceParser caller;

    protected EclipseSourceAggregator(SourceParser caller) {
        this.caller = caller;
    }

}
