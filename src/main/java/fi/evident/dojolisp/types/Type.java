package fi.evident.dojolisp.types;

import java.util.*;

public abstract class Type {

    Type() { }
    
    public static final Type OBJECT = basicType("Object");
    public static final Type UNIT = basicType("Unit");
    public static final Type INTEGER = basicType("Integer");
    public static final Type BOOLEAN = basicType("Boolean");
    public static final Type STRING = basicType("String");

    public static Type fromClass(Class<?> type) {
        return (type == Void.class)                             ? UNIT
             : (type == Object.class)                           ? OBJECT
             : (type == Boolean.class || type == boolean.class) ? BOOLEAN
             : (type == Integer.class || type == int.class)     ? INTEGER
             : (type == String.class)                           ? STRING
             : basicType(type.getName());
    }

    private static Type basicType(String name) {
        return new TypeConstructor(name, Kind.STAR);
    }

    public static Type arrayOf(Type type) {
        return new TypeApplication(new TypeConstructor("[]", Kind.ofParams(1)), type);
    }
    
    public static TypeScheme forName(String name) {
        return new TypeScheme(basicType(name));
    }

    public static Type makeFunctionType(List<Type> argumentTypes, Type returnType) {
        Type type = new TypeConstructor("->", Kind.ofParams(argumentTypes.size() + 1));

        for (Type argumentType : argumentTypes)
            type = new TypeApplication(type, argumentType);

        type = new TypeApplication(type, returnType);

        return type;
    }

    protected abstract Type apply(Substitution substitution);
    protected abstract Type instantiate(List<TypeVariable> vars);
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
