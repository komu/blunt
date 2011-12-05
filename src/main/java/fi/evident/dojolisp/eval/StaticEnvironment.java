package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Symbol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static fi.evident.dojolisp.types.Symbol.symbol;

public final class StaticEnvironment {

    private final StaticEnvironment parent;
    private final Set<Symbol> variables = new HashSet<Symbol>();

    public StaticEnvironment() {
        this(null);
    }

    public StaticEnvironment(StaticEnvironment parent) {
        this.parent = parent;
    }

    public boolean contains(Symbol var) {
        return variables.contains(var) || (parent != null && parent.contains(var));
    }

    public void define(String var) {
        define(symbol(var));
    }
    
    public void define(Symbol var) {
        if (!variables.add(var)) {
            throw new AnalyzationException("Variable " + var + " is already defined in this scope.");
        }
    }

    public void defineAll(Collection<Symbol> variables) {
        for (Symbol var : variables)
            define(var);
    }
}
