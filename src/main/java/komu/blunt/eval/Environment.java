package komu.blunt.eval;

public abstract class Environment {
    
    public final Object lookup(VariableReference var) {
        return lookup(var.frame, var.offset);
    }

    public final void set(VariableReference var, Object value) {
        set(var.frame, var.offset, value);
    }

    protected abstract void set(int frame, int offset, Object value);

    protected abstract Object lookup(int frame, int offset);

    public Environment extend(Object[] args) {
        return new NestedEnvironment(args, this);
    }
}
