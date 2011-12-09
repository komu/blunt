package fi.evident.dojolisp.objects;

import java.lang.reflect.Array;
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
        Object[] realArgs = Arrays.copyOf(args, types.length);
        realArgs[realArgs.length-1] = createVarArgArray(args, types);
        return realArgs;
    }

    private static Object createVarArgArray(Object[] args, Class<?>[] types) {
        int offset = types.length-1;
        Class<?> varArgType = types[offset].getComponentType();

        int len = args.length - offset;
        Object array = Array.newInstance(varArgType, len);
        
        if (varArgType.isPrimitive()) {
            for (int i = 0; i < len; i++)
                Array.set(array, i, Array.get(args, offset+i));
        } else {
            System.arraycopy(args, offset, array, 0, len);
        }

        return array;
    }

    @Override
    public String toString() {
        return "<#primitive procedure " + name + ">";
    }
}
