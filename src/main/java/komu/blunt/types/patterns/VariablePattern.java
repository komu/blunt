package komu.blunt.types.patterns;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VariablePattern extends Pattern {
    
    public final Symbol var;
    
    VariablePattern(Symbol var) {
        this.var = checkNotNull(var);
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
