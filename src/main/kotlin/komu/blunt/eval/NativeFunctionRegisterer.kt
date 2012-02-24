package komu.blunt.eval

object NativeFunctionRegisterer {
    /*
    private final RootBindings bindings;

    public NativeFunctionRegisterer(RootBindings bindings) {
        this.bindings = checkNotNull(bindings);
    }

    void register(Class<?> cl) {
        for (Method m : cl.getDeclaredMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null) {
                String name = func.value();
                Scheme type = resolveType(m);

                boolean isStatic = isStatic(m.getModifiers());
                bindings.bind(name, type, new PrimitiveFunction(name, m, isStatic));
            }
        }

        for (Field f : cl.getDeclaredFields()) {
            LibraryValue value = f.getAnnotation(LibraryValue.class);
            if (value != null) {
                Scheme scheme = Type.fromClass(f.getType()).toScheme();

                try {
                    bindings.bind(value.value(), scheme, f.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Scheme resolveType(Method m) {
        TypeScheme scheme = m.getAnnotation(TypeScheme.class);
        if (scheme != null)
            throw new UnsupportedOperationException("porting");
            //return TypeParser.$classobj.parseScheme(scheme.value());
        else
            return NativeTypeConversions.createFunctionType(m);

    }
    */
}

