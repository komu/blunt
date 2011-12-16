package fi.evident.dojolisp.types;

import fi.evident.dojolisp.eval.AnalyzationException;

import java.util.*;

public abstract class Type {

    private static final Map<String, Type> basicTypes = new HashMap<String, Type>();

    Type() { }
    
    public static final Type OBJECT = createOrGetBasic("Object");
    public static final Type UNIT = createOrGetBasic("Unit");
    public static final Type INTEGER = createOrGetBasic("Integer");
    public static final Type BOOLEAN = createOrGetBasic("Boolean");
    public static final Type STRING = createOrGetBasic("String");

    public static Type fromClass(Class<?> type) {
        return (type == Void.class)                             ? UNIT
             : (type == Object.class)                           ? OBJECT
             : (type == Boolean.class || type == boolean.class) ? BOOLEAN
             : (type == Integer.class || type == int.class)     ? INTEGER
             : (type == String.class)                           ? STRING
             : createOrGetBasic(type.getName());
    }

    private static Type createOrGetBasic(String name) {
        Type type = basicTypes.get(name);
        if (type == null) {
            type = new TypeConstructor(name, Kind.STAR);
            basicTypes.put(name, type);
        }
        return type;
    }

    public static Type arrayOf(Type type) {
        throw new UnsupportedOperationException("arrays are not yet supported");
    }
    
    public static TypeScheme forName(String name) {
        Type type = basicTypes.get(name);
        if (type != null)
            return new TypeScheme(type);
        else
            throw new AnalyzationException("unknown type: '" + name + "'");
    }

    public static Type makeFunctionType(List<Type> argumentTypes, Type returnType, boolean varArgs) {
        if (varArgs) throw new UnsupportedOperationException("varargs");

        Type type = new TypeConstructor("->", Kind.ofParams(argumentTypes.size() + 1));

        for (Type argumentType : argumentTypes)
            type = new TypeApplication(type, argumentType);

        type = new TypeApplication(type, returnType);

        return type;
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
