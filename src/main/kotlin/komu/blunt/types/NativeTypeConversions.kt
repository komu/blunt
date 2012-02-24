package komu.blunt.types

class NativeTypeConversions {
    /*
    private final Map<java.lang.reflect.TypeVariable<?>,TypeVariable> typeVariableMap = new HashMap<>();

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
            return quantifyAll(new Qualified<>(functionType(Type.UNIT, returnType)));
        else if (argumentTypes.size() == 1)
            return quantifyAll(new Qualified<>(functionType(argumentTypes.get(0), returnType)));
        else
            return quantifyAll(new Qualified<>(functionType(returnType, tupleType(argumentTypes))));
    }

    private List<Type> resolveArgumentTypes(Method m) {
        List<Type> result = new ArrayList<>(m.getParameterTypes().length + 1);

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
            var = typeVariable(tv.getName());
            typeVariableMap.put(tv, var);
        }
        return var;
    }

    private List<Type> resolveAll(java.lang.reflect.Type[] types) {
        List<Type> result = new ArrayList<>(types.length);

        for (java.lang.reflect.Type type : types)
            result.add(resolve(type));

        return result;
    }
    */
}
