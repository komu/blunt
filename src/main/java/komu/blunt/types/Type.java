package komu.blunt.types;

import static java.util.Arrays.asList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import komu.blunt.stdlib.TypeName;

public abstract class Type implements Types<Type> {

    Type() { }
    
    public static final Type UNIT = basicType("Unit");
    public static final Type BOOLEAN = basicType("Boolean");
    public static final Type INTEGER = basicType("Integer");
    public static final Type STRING = basicType("String");

    public static Type fromClass(Class<?> type) {
        return basicType(mapName(type));
    }
    
    private static String mapName(Class<?> type) {
        return (type == Void.class)                             ? "Unit"
             : (type == Boolean.class || type == boolean.class) ? "Boolean"
             : (type == BigInteger.class)                       ? "Integer"
             : (type == String.class)                           ? "String"
             : defaultMapName(type);
    }

    private static String defaultMapName(Class<?> type) {
        TypeName name = type.getAnnotation(TypeName.class);
        return name != null ? name.value() : type.getSimpleName();
    }

    private static Type basicType(String name) {
        return new TypeConstructor(name, Kind.STAR);
    }

    public static Type arrayOf(Type type) {
        return new TypeApplication(new TypeConstructor("[]", Kind.ofParams(1)), type);
    }
    
    public static Type functionType(Type argumentType, Type returnType) {
        return genericType("->", argumentType, returnType);
    }

    public static Type tupleType(Type... types) {
        return tupleType(asList(types));
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

    protected abstract Type instantiate(List<TypeVariable> vars);

    protected abstract Kind getKind();

    protected final Set<TypeVariable> getTypeVariables() {
        Set<TypeVariable> vars = new LinkedHashSet<TypeVariable>();
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
    
    public Scheme toScheme() {
        return new Scheme(Collections.<Kind>emptyList(), new Qualified<Type>(this));
    }
    
    public static List<Scheme> toSchemes(List<Type> ts) {
        List<Scheme> schemes = new ArrayList<Scheme>(ts.size());
        for (Type t : ts)
            schemes.add(t.toScheme());
        return schemes;
    }    
    
    protected abstract String toString(int precedence);

    public abstract boolean hnf();
}
