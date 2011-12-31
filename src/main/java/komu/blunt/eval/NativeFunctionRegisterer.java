package komu.blunt.eval;

import komu.blunt.objects.PrimitiveFunction;
import komu.blunt.stdlib.LibraryFunction;
import komu.blunt.stdlib.LibraryValue;
import komu.blunt.types.NativeTypeConversions;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.reflect.Modifier.isStatic;

final class NativeFunctionRegisterer {
    private final RootBindings bindings;

    public NativeFunctionRegisterer(RootBindings bindings) {
        this.bindings = checkNotNull(bindings);
    }

    void register(Class<?> cl) {
        for (Method m : cl.getDeclaredMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null) {
                String name = func.value();
                Scheme type = NativeTypeConversions.createFunctionType(m);

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
}
