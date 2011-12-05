package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.objects.Symbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static fi.evident.dojolisp.objects.Symbol.symbol;

public final class StaticEnvironment {

    private final StaticEnvironment parent;
    private final Map<Symbol, Integer> variables = new HashMap<Symbol, Integer>();

    public StaticEnvironment() {
        this(null);
    }

    public StaticEnvironment(StaticEnvironment parent) {
        this.parent = parent;
    }

    public VariableReference lookup(Symbol var) {
        return lookup(var, 0);
    }

    public VariableReference lookup(Symbol var, int depth) {
        Integer ref = variables.get(var);
        if (ref != null)
            return new VariableReference(depth, ref);
        else if (parent != null)
            return parent.lookup(var, depth+1);
        else
            throw new UnboundVariableException(var);
    }

    public VariableReference define(String var) {
        return define(symbol(var));
    }
    
    public VariableReference define(Symbol var) {
        if (variables.containsKey(var))
            throw new AnalyzationException("Variable " + var + " is already defined in this scope.");

        int offset = variables.size();
        variables.put(var, offset);
        return new VariableReference(0, offset);
    }

    public void defineAll(Collection<Symbol> variables) {
        for (Symbol var : variables)
            define(var);
    }
}
