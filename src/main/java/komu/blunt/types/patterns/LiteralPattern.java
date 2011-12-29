package komu.blunt.types.patterns;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LiteralPattern extends Pattern {

    private final Object value;

    public LiteralPattern(Object value) {
        this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
