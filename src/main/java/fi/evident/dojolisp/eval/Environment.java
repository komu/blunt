package fi.evident.dojolisp.eval;

public final class Environment {
    
    private final Object[] bindings;
    private final Environment parent;

    public Environment() {
        // TODO: currently there's a static limit of 1024 bindings for root-env, make this dynamic
        this(1024, null);
    }

    public Environment(int size, Environment parent) {
        this.parent = parent;
        this.bindings = new Object[size];
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
}
