package komu.blunt.eval;

import komu.blunt.analyzer.VariableReference;

public abstract class Environment {
    
    public final Object lookup(VariableReference var) {
        return lookup(var.frame, var.offset);
    }

    public final void set(VariableReference var, Object value) {
        set(var.frame, var.offset, value);
    }

    protected abstract void set(int frame, int offset, Object value);

    protected abstract Object lookup(int frame, int offset);

    public Environment extend(int envSize, Object arg) {
        Object[] args = new Object[envSize];
        args[0] = arg;
        return new NestedEnvironment(args, this);
    }
}
