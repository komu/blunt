package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

public abstract class Type {
    
    Type() { }
    
    public static final Type ANY = BasicType.createOrGet("Any");
    public static final Type UNIT = BasicType.createOrGet("Unit");
    public static final Type INTEGER = BasicType.createOrGet("Integer");
    public static final Type BOOLEAN = BasicType.createOrGet("Boolean");
    public static final Type STRING = BasicType.createOrGet("String");

    public static Type fromClass(Class<?> type) {
        return (type == Void.class)                             ? UNIT
             : (type == Object.class)                           ? ANY
             : (type == Boolean.class || type == boolean.class) ? BOOLEAN
             : (type == Integer.class || type == int.class)     ? INTEGER
             : (type == String.class)                           ? STRING
             : BasicType.createOrGet(type.getName());
    }
    
    public static Type arrayOf(Type type) {
        throw new UnsupportedOperationException("arrays are not yet supported");
    }
    
    public static Type forName(String name) {
        return BasicType.get(name);
    }

    public FunctionType asFunctionType() {
        throw new TypeCheckException("not a function type: " + this);
    }
}
