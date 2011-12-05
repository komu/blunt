package fi.evident.dojolisp.stdlib;

import fi.evident.dojolisp.eval.StaticBinding;
import fi.evident.dojolisp.objects.PrimitiveFunction;
import fi.evident.dojolisp.utils.Objects;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    public static void register(List<StaticBinding> bindings) {
        for (Method m : BasicFunctions.class.getMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null && Modifier.isStatic(m.getModifiers())) {
                String name = func.value();
                
                bindings.add(new StaticBinding(name, new PrimitiveFunction(name, m)));
            }
        }
    }

    @LibraryFunction("+")
    public static Number plus(Number... xs) {
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
