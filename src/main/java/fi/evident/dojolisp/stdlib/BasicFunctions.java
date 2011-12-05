package fi.evident.dojolisp.stdlib;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.types.PrimitiveFunction;
import fi.evident.dojolisp.utils.Objects;

import java.lang.reflect.Method;

public class BasicFunctions {
    
    public static void register(Environment env) {
        for (Method m : BasicFunctions.class.getMethods()) {
            fi.evident.dojolisp.stdlib.PrimitiveFunction func = m.getAnnotation(fi.evident.dojolisp.stdlib.PrimitiveFunction.class);
            if (func != null) {
                env.define(func.value(), new PrimitiveFunction(func.value(), m));
            }
        }
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction("+")
    public static Number plus(Number... xs) {
        int sum = 0;
                 
        for (Number x : xs)
            sum += x.intValue();
        
        return sum;
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction("<")
    public static boolean lt(int x, int y) {
        return x < y;
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction(">")
    public static boolean gt(int x, int y) {
        return x > y;
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction("<=")
    public static boolean le(int x, int y) {
        return x <= y;
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction(">=")
    public static boolean ge(int x, int y) {
        return x >= y;
    }

    @fi.evident.dojolisp.stdlib.PrimitiveFunction("=")
    public static boolean equal(Object x, Object... ys) {
        for (Object y : ys)
            if (!Objects.equal(x, y))
                return false;

        return true;
    }
}
