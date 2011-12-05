package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Symbol;

import java.util.HashMap;
import java.util.Map;

import static fi.evident.dojolisp.types.Symbol.symbol;
import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Environment {
    
    private final Map<Symbol, Object> bindings = new HashMap<Symbol, Object>();
    private final Environment parent;

    public Environment() {
        this(null);
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Object lookup(Symbol var) {
        Object value = bindings.get(var);
        if (value != null)
            return value;
        else if (bindings.containsKey(var))
            return null;
        else if (parent != null)
            return parent.lookup(var);
        else
            throw new RuntimeException("no binding for " + var);
    }

    public void define(Symbol var, Object value) {
        bindings.put(requireNonNull(var), value);
    }

    public void define(String var, Object value) {
        define(symbol(var), value);
    }
}
