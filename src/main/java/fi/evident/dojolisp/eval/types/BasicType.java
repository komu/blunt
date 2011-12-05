package fi.evident.dojolisp.eval.types;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class BasicType extends Type {
    
    private final String name;
    
    BasicType(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        
        if (o instanceof BasicType) {
            BasicType rhs = (BasicType) o;
            return name.equals(rhs.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
