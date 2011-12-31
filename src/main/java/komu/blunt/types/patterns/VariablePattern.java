package komu.blunt.types.patterns;

import komu.blunt.objects.Symbol;

public final class VariablePattern extends Pattern {
    
    public final Symbol var;
    
    VariablePattern(String var) {
        this.var = Symbol.symbol(var);
    }

    @Override
    public <R, C> R accept(PatternVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return var.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof VariablePattern) {
            VariablePattern rhs = (VariablePattern) obj;

            return var.equals(rhs.var);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return var.hashCode();
    }
}
