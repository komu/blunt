package komu.blunt.analyzer;

import komu.blunt.objects.Symbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

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
            return new VariableReference(depth, var.offset, name);
        else if (parent != null)
            return parent.lookup(name, depth+1);
        else
            throw new UnboundVariableException(name);
    }

    public VariableReference define(Symbol name) {
        if (variables.containsKey(name))
            throw new AnalyzationException("Variable " + name + " is already defined in this scope.");

        int offset = variables.size();
        variables.put(name, new VariableInfo(name, offset));
        return new VariableReference(0, offset, name);
    }
    
    public VariableReference lookupInCurrentScopeOrDefine(Symbol symbol) {
        VariableInfo var = variables.get(symbol);
        
        if (var != null) {
            return new VariableReference(0, var.offset, var.name);
        } else {
            return define(symbol);
        }
    }

    public StaticEnvironment extend(Symbol symbol) {
        return extend(asList(symbol));
    }

    public StaticEnvironment extend() {
        return new StaticEnvironment(this);
    }

    public StaticEnvironment extend(List<Symbol> symbols) {
        StaticEnvironment env = new StaticEnvironment(this);

        for (Symbol symbol : symbols)
            env.define(symbol);

        return env;
    }

    public int size() {
        return variables.size();
    }
}
