package komu.blunt.types;

import java.math.BigInteger;
import java.util.*;

import static java.util.Arrays.asList;

public abstract class Type implements Types<Type> {

    Type() { }
    
    public static final Type UNIT = basicType("Unit");
    public static final Type BOOLEAN = basicType("Boolean");
    public static final Type INTEGER = basicType("Integer");
    public static final Type STRING = basicType("String");

    private static String mapName(Class<?> type) {
        return (type == Void.class)                             ? "Unit"
             : (type == Boolean.class || type == boolean.class) ? "Boolean"
             : (type == BigInteger.class)                       ? "Integer"
             : (type == String.class)                           ? "String"
             : type.getSimpleName();
    }

    private static Type basicType(String name) {
        return new TypeConstructor(name, Kind.STAR);
    }

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
