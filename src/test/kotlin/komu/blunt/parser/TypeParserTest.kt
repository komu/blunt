package komu.blunt.parser

import komu.blunt.types.BasicType
import komu.blunt.types.Qualified
import komu.blunt.types.Type
import komu.blunt.types.isIn
import org.junit.Test
import kotlin.test.assertEquals

class TypeParserTest {

    @Test
    fun typeVariables() {
        verifyParsing("a", Type.Var("a"))
        verifyParsing("foo", Type.Var("foo"))
        verifyParsing("(a)", Type.Var("a"))
    }

    @Test
    fun functionTypes() {
        verifyParsing("a -> b", Type.function(Type.Var("a"), Type.Var("b")))
        verifyParsing("a -> b -> c", Type.function(Type.Var("a"), Type.function(Type.Var("b"), Type.Var("c"))))
        verifyParsing("a -> (b -> c)", Type.function(Type.Var("a"), Type.function(Type.Var("b"), Type.Var("c"))))
        verifyParsing("(a -> b) -> c", Type.function(Type.function(Type.Var("a"), Type.Var("b")), Type.Var("c")))
    }
    
    @Test
    fun unitType()  {
        verifyParsing("()", BasicType.UNIT)
    }

    @Test
    fun tupleTypes()  {
        verifyParsing("(a, b, c)", Type.tuple(Type.Var("a"), Type.Var("b"), Type.Var("c")))
    }

    @Test
    fun primitiveTypes() {
        verifyParsing("Foo", Type.generic("Foo"))
    }

    @Test
    fun genericTypes() {
        verifyParsing("Foo a", Type.generic("Foo", Type.Var("a")))
        verifyParsing("Foo Bar a", Type.generic("Foo", Type.generic("Bar"), Type.Var("a")))
        verifyParsing("Foo (Bar a)", Type.generic("Foo", Type.generic("Bar", Type.Var("a"))))
    }

    @Test
    fun listTypes() {
        verifyParsing("[a]", Type.list(Type.Var("a")))
    }

    @Test
    fun parseQualified() {
        val a = Type.Var("a")
        val b = Type.Var("b")

        verifyParsing("Num a => a -> a", Qualified(listOf(isIn("Num", a)), Type.function(a, a)))
        verifyParsing("(Num a) => a -> a", Qualified(listOf(isIn("Num", a)), Type.function(a, a)))
        verifyParsing("(Num a, Ord a, Num b) => a -> b", Qualified(listOf(isIn("Num", a), isIn("Ord", a), isIn("Num", b)), Type.function(a, b)))
    }
    
    @Test
    fun parseQualifiedWithoutQualifications() {
        verifyParsing("a -> a", Qualified.simple(Type.function(Type.Var("a"), Type.Var("a"))))
    }

    private fun verifyParsing(s: String, type: Qualified<Type>) {
        assertEquals(type, TypeParser.parseQualified(s))
    }

    private fun verifyParsing(s: String, type: Type) {
        assertEquals(type, TypeParser.parseType(s))
    }
}
