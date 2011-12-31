package komu.blunt.stdlib;

import com.google.common.base.Objects;
import komu.blunt.objects.EvaluationException;

import java.math.BigInteger;

@SuppressWarnings("unused")
public class BasicFunctions {
    
    @LibraryFunction("primitiveOpPlus")
    @TypeScheme("Num a => (a,a) -> a")
    public static BigInteger plus(BigInteger x, BigInteger y) {
        return x.add(y);
    }
    
    @LibraryFunction("primitiveOpMinus")
    @TypeScheme("Num a => (a,a) -> a")
    public static BigInteger minus(BigInteger x, BigInteger y) {
        return x.subtract(y);
    }

    @LibraryFunction("primitiveOpMultiply")
    @TypeScheme("Num a => (a,a) -> a")
    public static BigInteger multiply(BigInteger x, BigInteger y) {
        return x.multiply(y);
    }

    @LibraryFunction("primitiveOpDivide")
    @TypeScheme("Num a => (a,a) -> a")
    public static BigInteger divide(BigInteger x, BigInteger y) {
        return x.divide(y);
    }

    @LibraryFunction("primitiveMod")
    @TypeScheme("Num a => (a,a) -> a")
    public static BigInteger modulo(BigInteger x, BigInteger y) {
        return x.mod(y);
    }

    @LibraryFunction("primitiveOpLt")
    @TypeScheme("Ord a => (a,a) -> Boolean")
    public static <T extends Comparable<T>> boolean lt(T x, T y) {
        return x.compareTo(y) < 0;
    }

    @LibraryFunction("primitiveOpGt")
    @TypeScheme("Ord a => (a,a) -> Boolean")
    public static <T extends Comparable<T>> boolean gt(T x, T y) {
        return x.compareTo(y) > 0;
    }

    @LibraryFunction("primitiveOpLe")
    @TypeScheme("Ord a => (a,a) -> Boolean")
    public static <T extends Comparable<T>> boolean le(T x, T y) {
        return x.compareTo(y) <= 0;
    }

    @LibraryFunction("primitiveOpGe")
    @TypeScheme("Ord a => (a,a) -> Boolean")
    public static <T extends Comparable<T>> boolean ge(T x, T y) {
        return x.compareTo(y) >= 0;
    }

    @LibraryFunction("primitiveOpEq")
    @TypeScheme("Eq a => (a,a) -> Boolean")
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
}
