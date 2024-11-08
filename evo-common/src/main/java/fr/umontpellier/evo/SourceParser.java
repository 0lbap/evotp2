package fr.umontpellier.evo;

import java.nio.file.Path;
import java.util.function.Function;

public interface SourceParser {

    Path file();

    <T extends SourceAggregator.Result> T accept(SourceAggregator<T> agregator);

    default <T extends SourceAggregator.Result> T accept(Function<SourceParser, SourceAggregator<T>> supplier) {
        return accept(supplier.apply(this));
    }

}
