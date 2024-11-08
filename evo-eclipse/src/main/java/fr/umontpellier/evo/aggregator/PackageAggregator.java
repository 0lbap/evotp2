package fr.umontpellier.evo.aggregator;

import fr.umontpellier.evo.EclipseSourceParser;
import fr.umontpellier.evo.EclipseSourceAggregator;
import fr.umontpellier.evo.SourceParser;
import lombok.Data;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class PackageAggregator extends EclipseSourceAggregator<PackageAggregator.Result> {

    private final Result result = new Result();

    public PackageAggregator(SourceParser caller) {
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
    public class Result implements EclipseSourceAggregator.Result {
        private String pkg;
        private String name;
    }

}
