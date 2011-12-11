package fi.evident.dojolisp.stdlib;

import fi.evident.dojolisp.utils.Objects;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    @LibraryFunction("+")
    public static int plus(int... xs) {
        int sum = 0;
                 
        for (int x : xs)
            sum += x;
        
        return sum;
    }
    
    @LibraryFunction("-")
    public static int minus(int x, int... ys) {
        return ys.length == 0 ? -x : (x - plus(ys));
    }

    @LibraryFunction("*")
    public static int multiply(int... xs) {
        int product = 1;

        for (int x : xs)
            product *= x;

        return product;
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
