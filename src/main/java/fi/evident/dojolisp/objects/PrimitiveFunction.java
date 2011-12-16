package fi.evident.dojolisp.objects;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class PrimitiveFunction implements Function {

    private final Method method;
    private final String name;
    private final boolean isStatic;

    public PrimitiveFunction(String name, Method method, boolean isStatic) {
        this.name = requireNonNull(name);
        this.method = requireNonNull(method);
        this.isStatic = isStatic;
    }
    
    @Override
    public Object apply(Object[] args) {
        try {
            if (isStatic) {
                return method.invoke(null, prepareArgs(args));
            } else {
                Object receiver = args[0];
                args = Arrays.copyOfRange(args, 1, args.length);
                return method.invoke(receiver, prepareArgs(args));
            }

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
