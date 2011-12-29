package komu.blunt.parser;

import komu.blunt.objects.Symbol;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Operator {

    private static final Set<Operator> builtins = new HashSet<Operator>();
    
    public static final Operator EQ = builtin("==");
    public static final Operator LT = builtin("<");
    public static final Operator LE = builtin("<=");
    public static final Operator GT = builtin(">");
    public static final Operator GE = builtin(">=");
    public static final Operator PLUS = builtin("+");
    public static final Operator MINUS = builtin("-");
    public static final Operator MULTIPLY = builtin("*");
    public static final Operator DIVIDE = builtin("/");

    private static Operator builtin(String s) {
        Operator op = new Operator(s);
        builtins.add(op);
        return op;
    }

    public boolean isBuiltin() {
        return builtins.contains(this);
    }
    
    private final String name;

    public Operator(String name) {
        this.name = checkNotNull(name);
    }

    public Symbol toSymbol() {
        return Symbol.symbol(name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof Operator) {
            Operator rhs = (Operator) obj;
            
            return name.equals(rhs.name);
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isConstructor() {
        return name.startsWith(":");
    }
}
