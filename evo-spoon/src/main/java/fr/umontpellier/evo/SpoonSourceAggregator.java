package fr.umontpellier.evo;

import spoon.reflect.visitor.CtScanner;

public abstract class SpoonSourceAggregator<T extends SourceAggregator.Result> extends CtScanner implements SourceAggregator<T> {
}
