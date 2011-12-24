package komu.blunt.stdlib;

import com.google.common.base.Objects;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    @LibraryFunction("+")
    public static int plus(int x, int y) {
        return x + y;
    }
    
    @LibraryFunction("-")
    public static int minus(int x, int y) {
        return x - y;
    }

    @LibraryFunction("*")
    public static int multiply(int x, int y) {
        return x * y;
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
    public static <T> boolean equal(T x, T y) {
        return Objects.equal(x, y);
    }

    @LibraryFunction("unsafe-null")
    public static <T> T unsafeNull() {
        return null;
    }

    @LibraryFunction("fst")
    public static <A, B> A fst(A a, B b) {
        return a;
    }

    @LibraryFunction("snd")
    public static <A, B> B snd(A a, B b) {
        return b;
    }
}
