package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class StatisticVisitor extends ClassVisitor<StatisticVisitor.Result> {

    private final Result result = new Result(new ArrayList<>(), new ArrayList<>());

    public StatisticVisitor(ClassParser caller) {
        super(caller);
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

    public record Result(List<Method> methods, List<Field> fields) implements ClassVisitor.Result {
        public record Method(String name, int lineCount) {}
        public record Field() {}
    }

}
