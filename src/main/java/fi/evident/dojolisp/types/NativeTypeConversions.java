package fi.evident.dojolisp.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class NativeTypeConversions {

    public static Type createFunctionType(Method m) {
        // TODO: support parsing signatures from 'func'

        Map<java.lang.reflect.TypeVariable<?>,TypeVariable> typeVariableMap = new HashMap<java.lang.reflect.TypeVariable<?>, TypeVariable>();

        List<Type> argumentTypes = resolveArguments(m, typeVariableMap);
        Type returnType = resolve(typeVariableMap, m.getGenericReturnType());

        return new FunctionType(argumentTypes, returnType, m.isVarArgs());
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
            return Type.fromClass((Class<?>) type);
        } else if (type instanceof java.lang.reflect.TypeVariable<?>) {
            java.lang.reflect.TypeVariable<?> tv = (java.lang.reflect.TypeVariable<?>) type;
            TypeVariable var = typeVariableMap.get(tv);
            if (var == null) {
                var = new TypeVariable();
                typeVariableMap.put(tv, var);
            }
            return var;
        } else if (type instanceof GenericArrayType) {
            // TODO: implement generic arrays
            throw new IllegalArgumentException("array types not yet supported");
        } else
            throw new IllegalArgumentException("unsupported type: " + type.getClass());
    }
}
