package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.SpoonClassParser;
import fr.umontpellier.evo.SpoonClassVisitor;
import lombok.Data;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SpoonCouplageVisitor extends SpoonClassVisitor<SpoonCouplageVisitor.Result> {

    private final Result result = new Result();

    public SpoonCouplageVisitor(SpoonClassParser caller) {
        super(caller);
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        var parent = ((CtClass<?>) m.getParent()).getSimpleName();
        for (var statement: m.getBody().getStatements()) {
            if (statement instanceof CtInvocation<?>) {
                var invocation = (CtInvocation<?>) statement;
                var couple = Set.of(parent, invocation.getTarget().getType().getSimpleName());
                result.couplages.put(couple, result.couplages.getOrDefault(couple, 0) + 1);
            }
        }
        super.visitCtMethod(m);
    }

    @Override
    public Result result() {
        return result;
    }

    @Data
    public static class Result implements SpoonClassVisitor.Result {
        private final Map<Set<String>, Integer> couplages = new HashMap<>();
    }

}
