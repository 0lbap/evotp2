package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import lombok.Data;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticVisitor extends ClassVisitor<StatisticVisitor.Result> {

    private final Result result = new Result();

    public StatisticVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        result.pkg(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        var unit = this.caller.compilationUnit();
        result.clazz(new Result.Class(
                node.getName().getFullyQualifiedName(),
                unit.getLineNumber(node.getStartPosition() + node.getLength() - 1) - unit.getLineNumber(node.getStartPosition())
        ));
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        var unit = this.caller.compilationUnit();
        result.methods.add(new Result.Method(
                node.getName().getFullyQualifiedName(),
                unit.getLineNumber(node.getStartPosition() + node.getLength() - 1) - unit.getLineNumber(node.getStartPosition()),
                node.parameters().size()
        ));
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        result.fields.add(new Result.Field());
        return super.visit(node);
    }

    @Override
    public Result result() {
        return result;
    }

    /**
     * Les structures de données pour le résultat
     */
    @Data
    public static class Result implements ClassVisitor.Result {
        private String pkg;
        private Class clazz;
        private final List<Method> methods = new ArrayList<>();
        private final List<Field> fields = new ArrayList<>();

        @Data
        public static class Class {
            private final String name;
            private final int lineCount;
        }
        @Data
        public static class Method {
            private final String name;
            private final int lineCount;
            private final int parameters;
        }
        @Data
        public static class Field {}

    }

}
