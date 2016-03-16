package komu.blunt.stdlib

import komu.blunt.eval.RootBindings
import komu.blunt.objects.TypeConstructorValue
import java.math.BigInteger

@Retention(AnnotationRetention.RUNTIME)
annotation class LibraryFunction(val name: String, val scheme: String)

object BasicFunctions {

    fun register(bindings: RootBindings) {
        for (method in javaClass.methods) {
            val libraryFunc = method.getAnnotation(LibraryFunction::class.java)
            if (libraryFunc != null) {
                bindings.bindFunction(libraryFunc.name, libraryFunc.scheme, { s ->
                    if (s is TypeConstructorValue)
                        method.invoke(BasicFunctions, *s.items)
                    else
                        method.invoke(BasicFunctions, s)
                })
            }
        }
    }

    @LibraryFunction("primitiveCompare", "Ord a => (a,a) -> Ordering")
    fun <T : Comparable<T>> primitiveCompare(x: T, y: T): TypeConstructorValue {
        val r = x.compareTo(y)
        return when {
            r < 0 -> BasicValues.LT
            r > 0 -> BasicValues.GT
            else  -> BasicValues.EQ
        }
    }

    @LibraryFunction("show", "a -> String")
    fun show(s: Any): String = s.toString()

    @LibraryFunction("error", "String -> a")
    fun error(s: Any) = throw Exception(s.toString())

    @LibraryFunction("print", "a -> Unit")
    fun primitivePrint(s: Any) = print(s)

    @LibraryFunction("primitiveOpEq", "Eq a => (a,a) -> Boolean")
    fun primitiveOpEq(x: Any, y: Any): TypeConstructorValue = booleanToConstructor(x == y)

    @LibraryFunction("primitiveOpPlus", "Num a => (a,a) -> a")
    fun primitiveOpPlus(x: BigInteger, y: BigInteger): BigInteger = x + y

    @LibraryFunction("primitiveOpMinus", "Num a => (a,a) -> a")
    fun primitiveOpMinus(x: BigInteger, y: BigInteger): BigInteger = x - y

    @LibraryFunction("primitiveOpMultiply", "Num a => (a,a) -> a")
    fun primitiveOpMultiply(x: BigInteger, y: BigInteger): BigInteger = x * y

    @LibraryFunction("primitiveOpDivide", "Num a => (a,a) -> a")
    fun primitiveOpDivide(x: BigInteger, y: BigInteger): BigInteger = x / y

    @LibraryFunction("primitiveMod", "Num a => (a,a) -> a")
    fun primitiveOpMod(x: BigInteger, y: BigInteger): BigInteger = x % y
}
