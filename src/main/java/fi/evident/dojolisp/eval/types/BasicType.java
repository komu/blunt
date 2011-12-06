package fi.evident.dojolisp.eval.types;

import fi.evident.dojolisp.eval.AnalyzationException;

import java.util.HashMap;
import java.util.Map;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class BasicType extends Type {
    
    private final String name;

    private static final Map<String, BasicType> basicTypes = new HashMap<String, BasicType>();

    BasicType(String name) {
        this.name = requireNonNull(name);
    }

    static Type createOrGet(String name) {
        BasicType type = basicTypes.get(name);
        if (type == null) {
            type = new BasicType(name);
            basicTypes.put(name, type);
        }
        return type;
    }
    
    public static Type get(String name) {
        BasicType type = basicTypes.get(name);
        if (type != null)
            return type;
        else
            throw new AnalyzationException("unknown type: '" + name + "'");
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
