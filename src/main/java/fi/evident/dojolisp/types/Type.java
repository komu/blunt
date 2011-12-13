package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.TypeCheckException;

import java.util.*;

public abstract class Type {
    
    Type() { }
    
    public static final Type OBJECT = BasicType.createOrGet("Object");
    public static final Type UNIT = BasicType.createOrGet("Unit");
    public static final Type INTEGER = BasicType.createOrGet("Integer");
    public static final Type BOOLEAN = BasicType.createOrGet("Boolean");
    public static final Type STRING = BasicType.createOrGet("String");

    public static Type fromClass(Class<?> type) {
        return (type == Void.class)                             ? UNIT
             : (type == Object.class)                           ? OBJECT
             : (type == Boolean.class || type == boolean.class) ? BOOLEAN
             : (type == Integer.class || type == int.class)     ? INTEGER
             : (type == String.class)                           ? STRING
             : BasicType.createOrGet(type.getName());
    }
    
    public static Type arrayOf(Type type) {
        throw new UnsupportedOperationException("arrays are not yet supported");
    }
    
    public static TypeScheme forName(String name) {
        return new TypeScheme(BasicType.get(name));
    }

    public FunctionType asFunctionType() {
        throw new TypeCheckException("not a function type: " + this);
    }

    protected abstract Type apply(Substitution substitution);
    public abstract Type instantiate(List<TypeVariable> vars);
    protected abstract void addTypeVariables(Set<TypeVariable> result);
    
    protected abstract Kind getKind();

    protected final Set<TypeVariable> getTypeVariables() {
        Set<TypeVariable> vars = new HashSet<TypeVariable>();
        addTypeVariables(vars);
        return vars;
    }

    public TypeScheme quantifyAll() {
        return quantify(getTypeVariables());
    }

    public TypeScheme quantify(Collection<TypeVariable> variables) {
        List<TypeVariable> substitutedVariables = new ArrayList<TypeVariable>();
        for (TypeVariable v : getTypeVariables())
            if (variables.contains(v))
                substitutedVariables.add(v);

        List<Kind> kinds = new ArrayList<Kind>(substitutedVariables.size());
        for (TypeVariable v : substitutedVariables)
            kinds.add(v.getKind());

        Substitution substitution = new Substitution(substitutedVariables);

        return new TypeScheme(kinds, apply(substitution));
    }
}
