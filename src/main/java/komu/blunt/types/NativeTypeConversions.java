package komu.blunt.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Modifier.isStatic;

public final class NativeTypeConversions {

    private final Map<java.lang.reflect.TypeVariable<?>,TypeVariable> typeVariableMap = new HashMap<java.lang.reflect.TypeVariable<?>, TypeVariable>();

    private NativeTypeConversions() { }

    public static Scheme createFunctionType(Method m) {
        return new NativeTypeConversions().resolveFunctionType(m);
    }
    
    private Scheme resolveFunctionType(Method m) {
        if (m.isVarArgs())
            throw new IllegalArgumentException("varargs are not supported by type-system.");

        List<Type> argumentTypes = resolveArgumentTypes(m);
        Type returnType = resolve(m.getGenericReturnType());

        if (argumentTypes.isEmpty())
            return Type.functionType(Type.UNIT, returnType).quantifyAll();
        else if (argumentTypes.size() == 1)
            return Type.functionType(argumentTypes.get(0), returnType).quantifyAll();
        else
            return Type.functionType(Type.tupleType(argumentTypes), returnType).quantifyAll();
    }

    private List<Type> resolveArgumentTypes(Method m) {
        List<Type> result = new ArrayList<Type>(m.getParameterTypes().length + 1);

        // If the method is not static, the receiver is considered as an argument
        if (!isStatic(m.getModifiers()))
            result.add(resolve(m.getDeclaringClass()));

        for (java.lang.reflect.Type type : m.getGenericParameterTypes())
            result.add(resolve(type));

        return result;
    }

    private Type resolve(java.lang.reflect.Type type) {
        if (type instanceof Class<?>) {
            return resolveClass((Class<?>) type);

        } else if (type instanceof java.lang.reflect.TypeVariable<?>) {
            return variableFor((java.lang.reflect.TypeVariable<?>) type);

        } else if (type instanceof GenericArrayType) {
            return Type.arrayOf(resolve(((GenericArrayType) type).getGenericComponentType()));

        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            Class<?> ownerType = (Class<?>) pt.getRawType();
            List<Type> params = resolveAll(pt.getActualTypeArguments());
            
            return Type.genericType(ownerType, params);
        } else {
            throw new IllegalArgumentException("unsupported type: " + type.getClass());
        }
    }

    private Type resolveClass(Class<?> cl) {
        if (cl.isArray())
            return Type.arrayOf(resolve(cl.getComponentType()));
        else
            return Type.genericType(cl, resolveAll(cl.getTypeParameters()));
    }

    private TypeVariable variableFor(java.lang.reflect.TypeVariable<?> tv) {
        TypeVariable var = typeVariableMap.get(tv);
        if (var == null) {
            var = new TypeVariable(tv.getName(), Kind.STAR); // TODO: kind
            typeVariableMap.put(tv, var);
        }
        return var;
    }

    private List<Type> resolveAll(java.lang.reflect.Type[] types) {
        List<Type> result = new ArrayList<Type>(types.length);

        for (java.lang.reflect.Type type : types)
            result.add(resolve(type));

        return result;
    }
}
