package komu.blunt.objects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PrimitiveFunction implements Function {

    private final Method method;
    private final String name;
    private final boolean isStatic;

    public PrimitiveFunction(String name, Method method, boolean isStatic) {
        this.name = checkNotNull(name);
        this.method = checkNotNull(method);
        this.isStatic = isStatic;
    }
    
    @Override
    public Object apply(Object[] args) {
        try {
            if (isStatic) {
                return method.invoke(null, args);
            } else {
                return method.invoke(args[0], Arrays.copyOfRange(args, 1, args.length));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "<#primitive procedure " + name + ">";
    }
}
