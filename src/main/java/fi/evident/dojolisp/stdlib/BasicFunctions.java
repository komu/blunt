package fi.evident.dojolisp.stdlib;

import fi.evident.dojolisp.eval.StaticBinding;
import fi.evident.dojolisp.eval.types.FunctionType;
import fi.evident.dojolisp.eval.types.Type;
import fi.evident.dojolisp.objects.PrimitiveFunction;
import fi.evident.dojolisp.utils.Objects;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    public static void register(List<StaticBinding> bindings) {
        for (Method m : BasicFunctions.class.getMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null && Modifier.isStatic(m.getModifiers())) {
                String name = func.value();
                Type type = createFunctionType(func, m);
                bindings.add(new StaticBinding(name, type, new PrimitiveFunction(name, m)));
            }
        }
    }

    private static Type createFunctionType(LibraryFunction func, Method m) {
        // TODO: support parsing signatures from 'func'

        List<Type> argumentTypes = new ArrayList<Type>(m.getParameterTypes().length);
        for (Class<?> type : m.getParameterTypes())
            argumentTypes.add(Type.fromClass(type));
        
        Type returnType = Type.fromClass(m.getReturnType());
        
        if (m.isVarArgs()) {
            int last = argumentTypes.size()-1;
            argumentTypes.set(last, Type.fromClass(m.getParameterTypes()[last].getComponentType()));

            return new FunctionType(argumentTypes, returnType, true);
        } else {
            return new FunctionType(argumentTypes, returnType, false);
        }
    }

    @LibraryFunction("+")
    public static int intPlus(int x, int y) {
        return x + y;
    }
    
    @LibraryFunction("++")
    public static int plus(int... xs) {
        int sum = 0;
                 
        for (Number x : xs)
            sum += x.intValue();
        
        return sum;
    }

    @LibraryFunction("<")
    public static boolean lt(int x, int y) {
        return x < y;
    }

    @LibraryFunction(">")
    public static boolean gt(int x, int y) {
        return x > y;
    }

    @LibraryFunction("<=")
    public static boolean le(int x, int y) {
        return x <= y;
    }

    @LibraryFunction(">=")
    public static boolean ge(int x, int y) {
        return x >= y;
    }

    @LibraryFunction("=")
    public static boolean equal(Object x, Object... ys) {
        for (Object y : ys)
            if (!Objects.equal(x, y))
                return false;

        return true;
    }
}
