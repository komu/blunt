package komu.blunt.parser

import komu.blunt.types.*
import org.junit.Test
import kotlin.test.assertEquals

class TypeParserTest {

    @Test
    fun typeVariables() {
        verifyParsing("a", typeVariable("a"))
        verifyParsing("foo", typeVariable("foo"))
        verifyParsing("(a)", typeVariable("a"))
    }

    @Test
    fun functionTypes() {
        verifyParsing("a -> b", functionType(typeVariable("a"), typeVariable("b")))
        verifyParsing("a -> b -> c", functionType(typeVariable("a"), functionType(typeVariable("b"), typeVariable("c"))))
        verifyParsing("a -> (b -> c)", functionType(typeVariable("a"), functionType(typeVariable("b"), typeVariable("c"))))
        verifyParsing("(a -> b) -> c", functionType(functionType(typeVariable("a"), typeVariable("b")), typeVariable("c")))
    }
    
    @Test
    fun unitType()  {
        verifyParsing("()", BasicType.UNIT)
    }

    @Test
    fun tupleTypes()  {
        verifyParsing("(a, b, c)", tupleType(typeVariable("a"), typeVariable("b"), typeVariable("c")))
    }

    @Test
    fun primitiveTypes() {
        verifyParsing("Foo", genericType("Foo"))
    }

    @Test
    fun genericTypes() {
        verifyParsing("Foo a", genericType("Foo", typeVariable("a")))
        verifyParsing("Foo Bar a", genericType("Foo", genericType("Bar"), typeVariable("a")))
        verifyParsing("Foo (Bar a)", genericType("Foo", genericType("Bar", typeVariable("a"))))
    }

    @Test
    fun listTypes() {
        verifyParsing("[a]", listType(typeVariable("a")))
    }

    @Test
    fun parseQualified() {
        val a = typeVariable("a")
        val b = typeVariable("b")

        verifyParsing("Num a => a -> a", Qualified(listOf(isIn("Num", a)), functionType(a, a)))
        verifyParsing("(Num a) => a -> a", Qualified(listOf(isIn("Num", a)), functionType(a, a)))
        verifyParsing("(Num a, Ord a, Num b) => a -> b", Qualified(listOf(isIn("Num", a), isIn("Ord", a), isIn("Num", b)), functionType(a, b)))
    }
    
    @Test
    fun parseQualifiedWithoutQualifications() {
        verifyParsing("a -> a", Qualified.simple(functionType(typeVariable("a"), typeVariable("a"))))
    }

    private fun verifyParsing(s: String, type: Qualified<Type>) {
        assertEquals(type, TypeParser.parseQualified(s))
    }

    private fun verifyParsing(s: String, type: Type) {
        assertEquals(type, TypeParser.parseType(s))
    }
}
