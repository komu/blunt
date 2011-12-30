package komu.blunt.core;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreAlternative {

    public final CoreExpression extractor;
    public final CoreExpression body;

    public CoreAlternative(CoreExpression extractor, CoreExpression body) {
        this.extractor = checkNotNull(extractor);
        this.body = checkNotNull(body);
    }
}
