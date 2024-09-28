package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class StatisticVisitor extends ClassVisitor<StatisticVisitor.Result> {

    private final Result result = new Result();

    public StatisticVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        var unit = this.caller.compilationUnit();
        result.clazz(new Result.Class(
                unit.getLineNumber(node.getStartPosition() + node.getLength() - 1) - unit.getLineNumber(node.getStartPosition())
        ));
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        var unit = this.caller.compilationUnit();
        result.methods.add(new Result.Method(
                node.getName().getFullyQualifiedName(),
                unit.getLineNumber(node.getStartPosition() + node.getLength() - 1) - unit.getLineNumber(node.getStartPosition())
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

    public static class Result implements ClassVisitor.Result {
        private Class clazz;
        private final List<Method> methods = new ArrayList<>();
        private final List<Field> fields = new ArrayList<>();

        public record Class(int lineCount) {}
        public record Method(String name, int lineCount) {}
        public record Field() {}

        public List<Field> fields() {
            return fields;
        }

        public List<Method> methods() {
            return methods;
        }

        public Class clazz() {
            return clazz;
        }

        public Result clazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }
    }

}
