package komu.blunt.parser

import komu.blunt.types.ConstructorNames.CONS
import komu.blunt.types.ConstructorNames.NIL
import komu.blunt.types.ConstructorNames.UNIT
import komu.blunt.types.patterns.Pattern
import komu.blunt.types.patterns.Pattern.*
import org.junit.Test
import java.math.BigInteger
import kotlin.test.assertEquals

class PatternParserTest {

    @Test
    fun variablePatterns() {
        assertParse("a", Variable("a"))
        assertParse("foo", Variable("foo"))
    }

    @Test
    fun wildcardPattern() {
        assertParse("_", Wildcard)
    }

    @Test
    fun intLiterals() {
        assertParse("42", Literal(BigInteger.valueOf(42)))
    }

    @Test
    fun stringLiteral() {
        assertParse("\"foo\"", Literal("foo"))
    }

    @Test
    fun unitPattern() {
        assertParse("()", Constructor(UNIT))
    }

    @Test
    fun consPatterns() {
        assertParse("[]", Constructor(NIL))
        assertParse("x:y", Constructor(CONS, Variable("x"), Variable("y")))
        assertParse("x:y:z",
                Constructor(CONS, Variable("x"),
                        Constructor(CONS, Variable("y"),
                                Variable("z"))))
    }

    @Test
    fun listPatterns() {
        assertParse("[x, y, z]",
                Constructor(CONS, Variable("x"),
                        Constructor(CONS, Variable("y"),
                                Constructor(CONS, Variable("z"),
                                        Constructor(NIL)))))
    }

    @Test
    fun tuplePatterns() {
        assertParse("(x,y)", Constructor("(,)", Variable("x"), Variable("y")))
        assertParse("(x,y,z)", Constructor("(,,)", Variable("x"), Variable("y"), Variable("z")))
        assertParse("(x,y,z,w)", Constructor("(,,,)", Variable("x"), Variable("y"), Variable("z"), Variable("w")))
    }

    @Test
    fun constructorPattern() {
        assertParse("Foo", Constructor("Foo"))
        assertParse("Foo x y", Constructor("Foo", Variable("x"), Variable("y")))
        assertParse("Foo Bar Baz", Constructor("Foo", Constructor("Bar"), Constructor("Baz")))
    }

    private fun assertParse(source: String, expected: Pattern) {
        assertEquals(expected, PatternParser(Lexer(source)).parsePattern())
    }
}

