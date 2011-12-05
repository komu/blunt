package fi.evident.dojolisp.stdlib;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.StaticEnvironment;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.types.PrimitiveFunction;
import fi.evident.dojolisp.utils.Objects;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    public static void register(StaticEnvironment staticEnv, Environment env) {
        for (Method m : BasicFunctions.class.getMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null && Modifier.isStatic(m.getModifiers())) {
                String name = func.value();
                VariableReference ref = staticEnv.define(name);
                env.set(ref, new PrimitiveFunction(name, m));
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
