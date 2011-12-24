package komu.blunt.stdlib;

import com.google.common.base.Objects;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    @LibraryFunction("+")
    public static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }
    
    @LibraryFunction("-")
    public static BigInteger minus(BigInteger x, BigInteger y) {
        return x.subtract(y);
    }

    @LibraryFunction("*")
    public static BigInteger multiply(BigInteger x, BigInteger y) {
        return x.multiply(y);
    }

    @LibraryFunction("/")
    public static BigInteger divide(BigInteger x, BigInteger y) {
        return x.divide(y);
    }

    @LibraryFunction("<")
    public static boolean lt(BigInteger x, BigInteger y) {
        return x.compareTo(y) < 0;
    }

    @LibraryFunction(">")
    public static boolean gt(BigInteger x, BigInteger y) {
        return x.compareTo(y) > 0;
    }

    @LibraryFunction("<=")
    public static boolean le(BigInteger x, BigInteger y) {
        return x.compareTo(y) <= 0;
    }

    @LibraryFunction(">=")
    public static boolean ge(BigInteger x, BigInteger y) {
        return x.compareTo(y) >= 0;
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
