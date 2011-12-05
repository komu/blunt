package fi.evident.dojolisp.eval.types;

import fi.evident.dojolisp.eval.AnalyzationException;
import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.HashMap;
import java.util.Map;

public abstract class Type {
    
    private static final Map<String, Type> basicTypes = new HashMap<String, Type>();
    Type() {
    }
    
    public static final Type UNIT = basicType("Unit");
    public static final Type INTEGER = basicType("Integer");
    public static final Type BOOLEAN = basicType("Boolean");

    private static Type basicType(String name) {
        Type type = basicTypes.get(name);
        if (type == null) {
            type = new BasicType(name);
            basicTypes.put(name, type);
        }
        return type;
    }

    public static Type forName(String name) {
        Type type = basicTypes.get(name);
        if (type != null)
            return type;
        else
            throw new AnalyzationException("unknown type: '" + name + "'");
    }

    public static Type fromClass(Class<?> type) {
        if (type == Void.class)
            return UNIT;
        if (type == Boolean.class || type == boolean.class)
            return BOOLEAN;
        if (type == Integer.class || type == int.class)
            return INTEGER;
        else
            return basicType(type.getName());
    }

    public Type unify(Type type) {
        if (this.equals(type))
            return type;
        else
            throw new TypeCheckException(this, type);
    }

    public FunctionType asFunctionType() {
        throw new TypeCheckException("not a function type: " + this);
    }
}
