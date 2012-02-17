package komu.blunt.types.patterns;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class LiteralPattern extends Pattern {

    public final Object value;

    LiteralPattern(Object value) {
        this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof LiteralPattern) {
            LiteralPattern rhs = (LiteralPattern) obj;
            
            return Objects.equal(value, rhs.value);
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
