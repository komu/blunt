package komu.blunt.parser;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Operator {

    private final String name;
    public final Associativity associativity;
    public final int precedence;

    Operator(String name, Associativity associativity, int precedence) {
        checkArgument(precedence >= 0);
        this.name = checkNotNull(name);
        this.associativity = checkNotNull(associativity);
        this.precedence = precedence;
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
