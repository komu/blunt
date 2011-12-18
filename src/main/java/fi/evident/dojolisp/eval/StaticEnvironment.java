package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeScheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StaticEnvironment {

    private final StaticEnvironment parent;
    private final Map<Symbol, VariableInfo> variables = new HashMap<Symbol, VariableInfo>();

    public StaticEnvironment() {
        this(null);
    }

    public StaticEnvironment(StaticEnvironment parent) {
        this.parent = parent;
    }

    public VariableReference lookup(Symbol var) {
        return lookup(var, 0);
    }

    private VariableReference lookup(Symbol name, int depth) {
        VariableInfo var = variables.get(name);
        if (var != null)
            return new VariableReference(depth, var.offset, var.type, name);
        else if (parent != null)
            return parent.lookup(name, depth+1);
        else
            throw new UnboundVariableException(name);
    }

    public VariableReference define(Symbol name, TypeScheme type) {
        if (variables.containsKey(name))
            throw new AnalyzationException("Variable " + name + " is already defined in this scope.");

        int offset = variables.size();
        variables.put(name, new VariableInfo(name, offset, type));
        return new VariableReference(0, offset, type, name);
    }

    @SuppressWarnings("unused")
    public void dump() {
        for (VariableInfo var : variables.values())
            System.out.printf("%-10s: %s\n", var.name, var.type);
    }

    private void define(Symbol name) {
        define(name, new TypeScheme(Type.UNIT));
    }

    public StaticEnvironment extend(List<Symbol> arguments) {
        StaticEnvironment env = new StaticEnvironment(this);

        for (Symbol symbol : arguments)
            env.define(symbol);

        return env;
    }
}
