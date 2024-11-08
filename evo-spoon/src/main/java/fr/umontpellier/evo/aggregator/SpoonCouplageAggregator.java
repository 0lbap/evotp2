package fr.umontpellier.evo.aggregator;

import fr.umontpellier.evo.SourceAggregator;
import fr.umontpellier.evo.SpoonSourceAggregator;
import lombok.Data;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SpoonCouplageAggregator extends SpoonSourceAggregator<SpoonCouplageAggregator.Result> {

    private final Result result = new Result();

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
    public SpoonCouplageAggregator.Result result() {
        return result;
    }

    @Data
    public static class Result implements SourceAggregator.Result {
        private final Map<Set<String>, Integer> couplages = new HashMap<>();
    }

}
