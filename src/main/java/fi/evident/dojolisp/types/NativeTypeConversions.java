package fi.evident.dojolisp.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;

public class NativeTypeConversions {

    public static TypeScheme createFunctionType(Method m) {
        Map<java.lang.reflect.TypeVariable<?>,TypeVariable> typeVariableMap = new HashMap<java.lang.reflect.TypeVariable<?>, TypeVariable>();

        List<Type> argumentTypes = new ArrayList<Type>();

        if (!isStatic(m.getModifiers()))
            argumentTypes.add(resolve(typeVariableMap, m.getDeclaringClass()));

        argumentTypes.addAll(resolveArguments(m, typeVariableMap));
        Type returnType = resolve(typeVariableMap, m.getGenericReturnType());

        if (m.isVarArgs())
            throw new UnsupportedOperationException("varargs are not supported by type-system.");

        return Type.makeFunctionType(argumentTypes, returnType).quantifyAll();
    }

    private static List<Type> resolveArguments(Method m, Map<java.lang.reflect.TypeVariable<?>, TypeVariable> typeVariableMap) {
        List<Type> result = new ArrayList<Type>(m.getGenericParameterTypes().length);

        List<java.lang.reflect.Type> argumentTypes = asList(m.getGenericParameterTypes());
        if (m.isVarArgs()) {
            for (java.lang.reflect.Type type : argumentTypes.subList(0, argumentTypes.size()-1))
                result.add(resolve(typeVariableMap, type));

            int last = argumentTypes.size()-1;
            result.add(resolve(typeVariableMap, componentType(argumentTypes.get(last))));
        } else {
            for (java.lang.reflect.Type type : argumentTypes)
                result.add(resolve(typeVariableMap, type));
        }

        return result;
    }
    
    private static java.lang.reflect.Type componentType(java.lang.reflect.Type type) {
        if (type instanceof GenericArrayType) {
            return ((GenericArrayType) type).getGenericComponentType();
        } else {
            return ((Class<?>) type).getComponentType();
        }
    }

    private static Type resolve(Map<java.lang.reflect.TypeVariable<?>, TypeVariable> typeVariableMap, java.lang.reflect.Type type) {
        if (type instanceof Class<?>) {
            Class<?> cl = (Class<?>) type;
            if (cl.isArray())
                return Type.arrayOf(resolve(typeVariableMap, cl.getComponentType()));
            else {
                List<Type> params = resolveAll(typeVariableMap, asList(cl.getTypeParameters()));

                return Type.genericType(cl.getSimpleName(), params);
            }

        } else if (type instanceof java.lang.reflect.TypeVariable<?>) {
            java.lang.reflect.TypeVariable<?> tv = (java.lang.reflect.TypeVariable<?>) type;
            TypeVariable var = typeVariableMap.get(tv);
            if (var == null) {
                var = new TypeVariable(tv.getName(), Kind.STAR); // TODO: kind
                typeVariableMap.put(tv, var);
            }
            return var;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            return Type.arrayOf(resolve(typeVariableMap, arrayType.getGenericComponentType()));
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            Class<?> ownerType = (Class<?>) pt.getRawType();
            List<Type> params = resolveAll(typeVariableMap, asList(pt.getActualTypeArguments()));
            
            return Type.genericType(ownerType.getSimpleName(), params);
        } else {
            throw new IllegalArgumentException("unsupported type: " + type.getClass());
        }
    }
    
    private static List<Type> resolveAll(Map<java.lang.reflect.TypeVariable<?>, TypeVariable> typeVariableMap, List<? extends java.lang.reflect.Type> types) {
        List<Type> result = new ArrayList<Type>(types.size());
        
        for (java.lang.reflect.Type type : types)
            result.add(resolve(typeVariableMap, type));
        
        return result;
    }
}
