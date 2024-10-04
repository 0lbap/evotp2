package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import lombok.Data;
import lombok.Getter;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallGraphVisitor extends ClassVisitor<CallGraphVisitor.Result> {

    private final Result result = new Result();
    private String method = "";

    public CallGraphVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        this.method = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getName() == null || node.getName().getFullyQualifiedName().isEmpty())
            return super.visit(node);

        if (result.calls.containsKey(method)) {
            result.calls.get(method).add(node.getName().getFullyQualifiedName());
        } else {
            result.calls.put(method, new ArrayList<>(List.of(node.getName().getFullyQualifiedName())));
        }
        return super.visit(node);
    }

    @Override
    public Result result() {
        return result;
    }

    @Getter
    @Data
    public static class Result implements ClassVisitor.Result {
        private final Map<String, List<String>> calls = new HashMap<>();
    }

}
