package komu.blunt.stdlib

import java.math.BigInteger
import komu.blunt.eval.RootBindings
import komu.blunt.objects.TypeConstructorValue
import kotlin.math.div

object BasicFunctions {

    fun register(bindings: RootBindings) {
        bindings.bindFunction("error", "String -> a") { s -> throw Exception(s.toString()) }
        bindings.bindFunction("show", "a -> String") { s -> s.toString() }
        bindings.bindFunction("print", "a -> Unit") { s -> print(s) }

        bindings.bindFunction("primitiveCompare", "Ord a => (a,a) -> Ordering") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as Comparable<Any?>
            val y = tc.items[1] as Comparable<Any?>

            val r = x.compareTo(y)
            if (r < 0)
                BasicValues.LT
            else if (r > 0)
                BasicValues.GT
            else
                BasicValues.EQ
        }

        bindings.bindFunction("primitiveOpEq", "Eq a => (a,a) -> Boolean") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0]
            val y = tc.items[1]

            x == y
        }

        bindings.bindFunction("primitiveOpPlus", "Num a => (a,a) -> a") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as BigInteger
            val y = tc.items[1] as BigInteger

            x + y
        }

        bindings.bindFunction("primitiveOpMinus", "Num a => (a,a) -> a") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as BigInteger
            val y = tc.items[1] as BigInteger

            x - y
        }

        bindings.bindFunction("primitiveOpMultiply", "Num a => (a,a) -> a") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as BigInteger
            val y = tc.items[1] as BigInteger

            x * y
        }

        bindings.bindFunction("primitiveOpDivide", "Num a => (a,a) -> a") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as BigInteger
            val y = tc.items[1] as BigInteger

            x / y
        }

        bindings.bindFunction("primitiveMod", "Num a => (a,a) -> a") { s ->
            val tc = s as TypeConstructorValue
            val x = tc.items[0] as BigInteger
            val y = tc.items[1] as BigInteger

            x % y
        }
    }

    //@LibraryFunction("primitiveOpPlus")
    //@TypeScheme("Num a => (a,a) -> a")
    //fun plus(x: BigInteger, y: BigInteger): BigInteger = x.add(y)

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

