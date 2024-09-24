package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import org.eclipse.jdt.core.dom.PackageDeclaration;

public class PackageResolverVisitor extends ClassVisitor<PackageResolverVisitor.Result> {

    private Result pkg;

    public PackageResolverVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        this.pkg = new Result(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public Result result() {
        return pkg;
    }

    public record Result(String pkg) implements ClassVisitor.Result { }

}
