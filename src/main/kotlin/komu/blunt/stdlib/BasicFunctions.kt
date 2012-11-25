package komu.blunt.stdlib

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.reflect.Method
import java.math.BigInteger
import komu.blunt.eval.RootBindings
import komu.blunt.objects.TypeConstructorValue
import kotlin.math.*

object BasicFunctions {

    Retention(RetentionPolicy.RUNTIME)
    annotation class libraryFunction(val name: String, val scheme: String)

    fun register(bindings: RootBindings) {
        val methods = javaClass.getMethods() as Array<Method>

        for (method in methods) {
            val libraryFunc = method.getAnnotation(javaClass<libraryFunction>())
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

    libraryFunction("primitiveCompare", "Ord a => (a,a) -> Ordering")
    fun primitiveCompare<T : Comparable<T>>(x: T, y: T): TypeConstructorValue {
        val r = x.compareTo(y)
        return if (r < 0)
            BasicValues.LT
        else if (r > 0)
            BasicValues.GT
        else
            BasicValues.EQ
    }

    libraryFunction("show", "a -> String")
    fun show(s: Any): String = s.toString()

    libraryFunction("error", "String -> a")
    fun error(s: Any) = throw Exception(s.toString())

    libraryFunction("print", "a -> Unit")
    fun primitivePrint(s: Any) = print(s)

    libraryFunction("primitiveOpEq", "Eq a => (a,a) -> Boolean")
    fun primitiveOpEq(x: Any, y: Any): TypeConstructorValue = booleanToConstructor(x == y)

    libraryFunction("primitiveOpPlus", "Num a => (a,a) -> a")
    fun primitiveOpPlus(x: BigInteger, y: BigInteger): BigInteger = x + y

    libraryFunction("primitiveOpMinus", "Num a => (a,a) -> a")
    fun primitiveOpMinus(x: BigInteger, y: BigInteger): BigInteger = x - y

    libraryFunction("primitiveOpMultiply", "Num a => (a,a) -> a")
    fun primitiveOpMultiply(x: BigInteger, y: BigInteger): BigInteger = x * y

    libraryFunction("primitiveOpDivide", "Num a => (a,a) -> a")
    fun primitiveOpDivide(x: BigInteger, y: BigInteger): BigInteger = x / y

    libraryFunction("primitiveMod", "Num a => (a,a) -> a")
    fun primitiveOpMod(x: BigInteger, y: BigInteger): BigInteger = x % y
}
