package fr.umontpellier.evo.visitor;

import fr.umontpellier.evo.ClassParser;
import fr.umontpellier.evo.ClassVisitor;
import lombok.Data;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class PackageResolverVisitor extends ClassVisitor<PackageResolverVisitor.Result> {

    private final Result result = new Result();

    public PackageResolverVisitor(ClassParser caller) {
        super(caller);
    }

    @Override
    public boolean visit(PackageDeclaration node) {
        this.result.pkg(node.getName().getFullyQualifiedName());
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        this.result.name(node.getName().toString());
        return super.visit(node);
    }

    @Override
    public Result result() {
        return result;
    }

    @Data
    public class Result implements ClassVisitor.Result {
        private String pkg;
        private String name;
    }

}
