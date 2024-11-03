package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import lombok.Data;
import lombok.Getter;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class CouplageVisitor extends ClassVisitor<CouplageVisitor.Result> {

    @Getter
    private final Result result = new Result();
    private final Stack<Map<String, String>> variableTypes = new Stack<>();
    private final Stack<String> currentClass = new Stack<>();

    public CouplageVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        currentClass.push(node.getName().getFullyQualifiedName());
        variableTypes.push(new HashMap<>());
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        variableTypes.push(new HashMap<>());
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        String type = removeGenerics(node.getType().toString());
        for (var fragment: node.fragments()) {
            if (fragment instanceof VariableDeclaration) {
                String name = ((VariableDeclaration) fragment).getName().getFullyQualifiedName();
                variableTypes.peek().put(name, type);
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        String type = removeGenerics(node.getType().toString());
        for (var fragment: node.fragments()) {
            if (fragment instanceof VariableDeclaration) {
                String name = ((VariableDeclaration) fragment).getName().getFullyQualifiedName();
                variableTypes.peek().put(name, type);
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        var type = "Unknown";
        for (var map: variableTypes) {
            if (node.getExpression() == null) break;
            if (map.containsKey(node.getExpression().toString())) {
                type = map.get(node.getExpression().toString());
                break;
            }
        }
        var source = currentClass.peek();
        var couple = Set.of(type, source);
        result.couplages.put(couple, result.couplages.getOrDefault(couple, 0) + 1);

        return super.visit(node);
    }

    private static String removeGenerics(String typeWithGenerics) {
        // Use a more advanced regex to handle nested generics
        while (typeWithGenerics.contains("<")) {
            typeWithGenerics = typeWithGenerics.replaceAll("<[^<>]*>", "");
        }
        return typeWithGenerics;
    }

    @Data
    public static class Result implements ClassVisitor.Result {
        private final Map<Set<String>, Integer> couplages = new HashMap<>();
    }

}
