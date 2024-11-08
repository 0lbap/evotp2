package fr.umontpellier.evo.aggregator;

import fr.umontpellier.evo.EclipseSourceParser;
import fr.umontpellier.evo.EclipseSourceAggregator;
import fr.umontpellier.evo.SourceParser;
import lombok.Data;
import lombok.Getter;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.*;

public class CallGraphAggregator extends EclipseSourceAggregator<CallGraphAggregator.Result> {

    private final Result result = new Result();
    private final Stack<String> method = new Stack<>(),
            clazz = new Stack<>();

    public CallGraphAggregator(SourceParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        this.clazz.push(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        this.method.push(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getName() == null || node.getName().getFullyQualifiedName().isEmpty())
            return super.visit(node);

        String source;
        try {
            source = clazz.peek() + "." + method.peek();
        } catch (Exception e) {
            source = clazz.peek();
        }

        if (result.calls.containsKey(source)) {
            result.calls.get(source).add(node.getName().getFullyQualifiedName());
        } else {
            result.calls.put(source, new ArrayList<>(List.of(node.getName().getFullyQualifiedName())));
        }
        return super.visit(node);
    }

    @Override
    public Result result() {
        return result;
    }

    /**
     * Résultat contient un dictionnaire avec les sources puis les appels
     */
    @Getter
    @Data
    public static class Result implements EclipseSourceAggregator.Result {
        private final Map<String, List<String>> calls = new HashMap<>();
    }

}
