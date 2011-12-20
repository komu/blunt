package komu.blunt.types;

import java.util.*;

import static java.util.Arrays.asList;

public abstract class Type {

    Type() { }
    
    public static final Type UNIT = basicType("Unit");
    public static final Type BOOLEAN = basicType("Boolean");

    public static Type fromClass(Class<?> type) {
        return basicType(mapName(type));
    }
    
    private static String mapName(Class<?> type) {
        return (type == Void.class)                             ? "Unit"
             : (type == Boolean.class || type == boolean.class) ? "Boolean"
             : (type == Integer.class || type == int.class)     ? "Integer"
             : type.getSimpleName();
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
        return genericType("->", tupleType(argumentTypes), returnType);
    }
    
    public static Type tupleType(List<Type> types) {
        return genericType(",", types);
    }
    
    public static Type genericType(Class<?> cl, List<Type> params) {
        return genericType(mapName(cl), params);
    }

    public static Type genericType(String name, Type... params) {
        return genericType(name, asList(params));
    }
    
    public static Type genericType(String name, List<Type> params) {
        Type type = new TypeConstructor(name, Kind.ofParams(params.size()));

        for (Type param : params)
            type = new TypeApplication(type, param);

        return type;
    }

    protected abstract Type apply(Substitution substitution);
    protected abstract Type instantiate(List<TypeVariable> vars);
    protected abstract void addTypeVariables(Set<TypeVariable> result);
    
    protected abstract Kind getKind();

    protected final Set<TypeVariable> getTypeVariables() {
        Set<TypeVariable> vars = new LinkedHashSet<TypeVariable>();
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
