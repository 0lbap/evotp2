package fr.umontpellier.evo;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtTypeInformation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class SpoonSourceLoader implements SourceLoader {

    public static final SpoonSourceLoader INSTANCE = new SpoonSourceLoader();

    @Override
    public List<SourceParser> load(Path path) throws IOException {
        var launcher = new Launcher();
        launcher.addInputResource(path.toString());
        var types = launcher.buildModel().getAllTypes();

        return types.stream()
                .filter(CtTypeInformation::isClass)
                .map(c -> (CtClass<?>) c)
                .map(c -> new SpoonSourceParser(path, c))
                .collect(Collectors.toList());
    }
}
