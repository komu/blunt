package komu.blunt.stdlib

import com.google.common.base.Objects
import komu.blunt.objects.EvaluationException
import komu.blunt.objects.TypeConstructorValue

import java.math.BigInteger
import komu.blunt.eval.RootBindings
import komu.blunt.types.Scheme
import komu.blunt.parser.TypeParser

object BasicFunctions {

    fun register(bindings: RootBindings) {
        bindings.bind("primitiveCompare", "Ord a => (a,a) -> Ordering", null)
    }

    //@LibraryFunction("primitiveOpPlus")
    //@TypeScheme("Num a => (a,a) -> a")
    fun plus(x: BigInteger, y: BigInteger): BigInteger = x.add(y).sure()

//    @LibraryFunction("primitiveOpMinus")
//    @TypeScheme("Num a => (a,a) -> a")
//    public static BigInteger minus(BigInteger x, BigInteger y) {
//        return x.subtract(y);
//    }
//
//    @LibraryFunction("primitiveOpMultiply")
//    @TypeScheme("Num a => (a,a) -> a")
//    public static BigInteger multiply(BigInteger x, BigInteger y) {
//        return x.multiply(y);
//    }
//
//    @LibraryFunction("primitiveOpDivide")
//    @TypeScheme("Num a => (a,a) -> a")
//    public static BigInteger divide(BigInteger x, BigInteger y) {
//        return x.divide(y);
//    }
//
//    @LibraryFunction("primitiveMod")
//    @TypeScheme("Num a => (a,a) -> a")
//    public static BigInteger modulo(BigInteger x, BigInteger y) {
//        return x.mod(y);
//    }
//
//    @LibraryFunction("primitiveOpEq")
//    @TypeScheme("Eq a => (a,a) -> Boolean")
//    public static <T> boolean equal(T x, T y) {
//        return Objects.equal(x, y);
//    }
//
//    @LibraryFunction("primitiveCompare")
//    @TypeScheme("Ord a => (a,a) -> Ordering")
//    public static <T extends Comparable<T>> TypeConstructorValue compare(T x, T y) {
//        int r = x.compareTo(y);
//        if (r < 0)
//            return BasicValues.LT;
//        else if (r > 0)
//            return BasicValues.GT;
//        else
//            return BasicValues.EQ;
//    }
//
//    @LibraryFunction("show")
//    public static <T> String show(T o) {
//        return String.valueOf(o);
//    }
//
//    @LibraryFunction("error")
//    public static <T> T error(String message) {
//        throw new EvaluationException(message);
//    }
//
//    @LibraryFunction("print")
//    public static <T> void print(T value) {
//        System.out.print(value);
//    }
}

