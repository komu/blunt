package fi.evident.dojolisp.eval;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Environment {
    
    private final Object[] bindings;
    private final Environment parent;

    public Environment(int size) {
        this.bindings = new Object[size];
        this.parent = null;
    }

    private Environment(Object[] bindings, Environment parent) {
        this.bindings = requireNonNull(bindings);
        this.parent = parent;
    }

    public Object lookup(VariableReference var) {
        return bindingsForFrame(var.frame)[var.offset];
    }

    public void set(VariableReference var, Object value) {
        bindingsForFrame(var.frame)[var.offset] = value;
    }

    private Object[] bindingsForFrame(int depth) {
        Environment env = this;
        for (int i = 0; i < depth; i++)
            env = env.parent;
        return env.bindings;
    }

    public Environment extend(Object[] args) {
        return new Environment(args, this);
    }
}
