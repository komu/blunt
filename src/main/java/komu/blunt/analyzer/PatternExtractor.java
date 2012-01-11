package komu.blunt.analyzer;

import komu.blunt.core.CoreExpression;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PatternExtractor {
    public final CoreExpression predicate;
    public final CoreExpression extractor;

    public PatternExtractor(CoreExpression predicate, CoreExpression extractor) {
        this.predicate = checkNotNull(predicate);
        this.extractor = checkNotNull(extractor);
    }
}
