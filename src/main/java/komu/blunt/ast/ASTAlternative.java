package komu.blunt.ast;

import komu.blunt.types.patterns.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTAlternative {

    public final Pattern pattern;
    public final ASTExpression value;

    ASTAlternative(Pattern pattern, ASTExpression value) {
        this.pattern = checkNotNull(pattern);
        this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
        return pattern + " -> " + value;
    }
}
