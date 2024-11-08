package fr.umontpellier.evo;

import lombok.Data;
import spoon.Launcher;
import spoon.SpoonException;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.CtVisitor;

import java.nio.file.Path;

@Data
public class SpoonSourceParser implements SourceParser {

    private final Path file;
    private final CtClass<?> clazz;

    SpoonSourceParser(Path file, CtClass<?> clazz) {
        this.file = file;
        this.clazz = clazz;
    }

    @Override
    public <T extends SourceAggregator.Result> T accept(SourceAggregator<T> aggregator) {
        clazz.accept((CtVisitor) aggregator);
        return aggregator.result();
    }

}
