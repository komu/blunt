package komu.blunt.types.patterns;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VariablePattern extends Pattern {
    
    public final Symbol var;
    
    public VariablePattern(Symbol var) {
        this.var = checkNotNull(var);
    }

    @Override
    public <R, C> R accept(PatternVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
