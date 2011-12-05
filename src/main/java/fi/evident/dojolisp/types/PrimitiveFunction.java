package fi.evident.dojolisp.types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class PrimitiveFunction implements Function {

    private final Method method;
    private final String name;
    private final Object receiver;

    public PrimitiveFunction(String name, Method method) {
        this(name, method, null);
    }

    public PrimitiveFunction(String name, Method method, Object receiver) {
        this.name = requireNonNull(name);
        this.method = requireNonNull(method);
        this.receiver = receiver;
    }
    
    @Override
    public Object apply(Object[] args) {
        try {
            return method.invoke(receiver, prepareArgs(args));

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Object[] prepareArgs(Object[] args) {
        return method.isVarArgs()
            ? prepareVarArgs(args, method.getParameterTypes())
            : args;
    }

    private static Object[] prepareVarArgs(Object[] args, Class<?>[] types) {
        Class<? extends Object[]> varArgType = types[types.length-1].asSubclass(Object[].class);

        Object[] realArgs = Arrays.copyOf(args, types.length);
        realArgs[realArgs.length-1] = Arrays.copyOfRange(args, types.length - 1, args.length, varArgType);

        return realArgs;
    }

    @Override
    public String toString() {
        return "<#primitive procedure " + name + ">";
    }
}
