package fi.evident.dojolisp.objects;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Symbol {
    
    private final String value;
    
    private Symbol(String value) {
        this.value = requireNonNull(value);
    }
    
    public static Symbol symbol(String symbol) {
        return new Symbol(symbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof Symbol) {
            Symbol rhs = (Symbol) o;
            return value.equals(rhs.value);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
