package fi.evident.dojolisp.types;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeTypeConversions {

    public static Type createFunctionType(Method m) {
        // TODO: support parsing signatures from 'func'

        Map<java.lang.reflect.TypeVariable<?>,TypeVariable> typeVariableMap = new HashMap<java.lang.reflect.TypeVariable<?>, TypeVariable>();
        
        List<Type> argumentTypes = new ArrayList<Type>(m.getGenericParameterTypes().length);
        for (java.lang.reflect.Type type : m.getGenericParameterTypes())
            argumentTypes.add(resolve(typeVariableMap, type));

        Type returnType = resolve(typeVariableMap, m.getGenericReturnType());

        if (m.isVarArgs()) {
            int last = argumentTypes.size()-1;
            argumentTypes.set(last, resolve(typeVariableMap, m.getParameterTypes()[last].getComponentType()));

            return new FunctionType(argumentTypes, returnType, true);
        } else {
            return new FunctionType(argumentTypes, returnType, false);
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
