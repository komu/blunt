package komu.blunt.types;

import java.util.*;

public abstract class Type implements Types<Type> {

    Type() { }
    
    protected abstract Type instantiate(List<TypeVariable> vars);

    public abstract Kind getKind();

    @Override
    public final String toString() {
        return toString(0);
    }
    
    protected abstract String toString(int precedence);

    public abstract boolean hnf();
}
