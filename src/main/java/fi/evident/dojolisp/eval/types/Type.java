package fi.evident.dojolisp.eval.types;

import fi.evident.dojolisp.eval.TypeCheckException;

public abstract class Type {
    
    Type() { }
    
    public static final Type UNIT = BasicType.createOrGet("Unit");
    public static final Type INTEGER = BasicType.createOrGet("Integer");
    public static final Type BOOLEAN = BasicType.createOrGet("Boolean");
    public static final Type STRING = BasicType.createOrGet("String");

    public static Type fromClass(Class<?> type) {
        return (type == Void.class)                             ? UNIT
             : (type == Boolean.class || type == boolean.class) ? BOOLEAN
             : (type == Integer.class || type == int.class)     ? INTEGER
             : (type == String.class)                           ? STRING
             : BasicType.createOrGet(type.getName());
    }
    
    public static Type forName(String name) {
        return BasicType.get(name);
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
