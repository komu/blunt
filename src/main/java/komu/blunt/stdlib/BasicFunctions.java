package komu.blunt.stdlib;

import com.google.common.base.Objects;
import komu.blunt.objects.EvaluationException;
import komu.blunt.objects.TypeConstructorValue;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    @LibraryFunction("primitiveOpPlus")
    public static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }
    
    @LibraryFunction("primitiveOpMinus")
    public static BigInteger minus(BigInteger x, BigInteger y) {
        return x.subtract(y);
    }

    @LibraryFunction("primitiveOpMultiply")
    public static BigInteger multiply(BigInteger x, BigInteger y) {
        return x.multiply(y);
    }

    @LibraryFunction("primitiveOpDivide")
    public static BigInteger divide(BigInteger x, BigInteger y) {
        return x.divide(y);
    }

    @LibraryFunction("primitiveMod")
    public static BigInteger modulo(BigInteger x, BigInteger y) {
        return x.mod(y);
    }

    @LibraryFunction("primitiveOpLt")
    public static <T extends Comparable<T>> boolean lt(T x, T y) {
        return x.compareTo(y) < 0;
    }

    @LibraryFunction("primitiveOpGt")
    public static <T extends Comparable<T>> boolean gt(T x, T y) {
        return x.compareTo(y) > 0;
    }

    @LibraryFunction("primitiveOpLe")
    public static <T extends Comparable<T>> boolean le(T x, T y) {
        return x.compareTo(y) <= 0;
    }

    @LibraryFunction("primitiveOpGe")
    public static <T extends Comparable<T>> boolean ge(T x, T y) {
        return x.compareTo(y) >= 0;
    }

    @LibraryFunction("primitiveOpEq")
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
    
    @LibraryFunction("show")
    public static <T> String show(T o) {
        return String.valueOf(o);
    }

    @LibraryFunction("error")
    public static <T> T error(String message) {
        throw new EvaluationException(message);
    }


    @LibraryFunction(value="just?", type="Maybe a -> Boolean")
    public static boolean isJust(TypeConstructorValue value) {
        return value.name.equals("Just");
    }

    @LibraryFunction(value="fromJust", type="Maybe a -> a")
    public static Object fromJust(TypeConstructorValue value) {
        if (isJust(value))
            return value.items[0];
        else
            return BasicFunctions.error("not just " + value);
    }
}
