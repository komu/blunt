package komu.blunt.types;

import java.math.BigInteger;
import java.util.*;

import static java.util.Arrays.asList;

public abstract class Type implements Types<Type> {

    Type() { }
    
    protected abstract Type instantiate(List<TypeVariable> vars);

    public abstract Kind getKind();

    protected final Set<TypeVariable> getTypeVariables() {
        Set<TypeVariable> vars = new LinkedHashSet<>();
        addTypeVariables(vars);
        return vars;
    }

    public boolean containsVariable(TypeVariable v) {
        return getTypeVariables().contains(v);
    }

    @Override
    public final String toString() {
        return toString(0);
    }
    
    protected abstract String toString(int precedence);

    public abstract boolean hnf();
}
