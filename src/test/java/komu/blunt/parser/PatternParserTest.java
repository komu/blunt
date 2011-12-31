package komu.blunt.parser;

import komu.blunt.types.patterns.Pattern;
import org.junit.Test;

import java.math.BigInteger;

import static komu.blunt.types.DataTypeDefinitions.*;
import static komu.blunt.types.patterns.Pattern.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PatternParserTest {

    @Test
    public void variablePatterns() {
        assertParse("a", variable("a"));
        assertParse("foo", variable("foo"));
    }

    @Test
    public void wildcardPattern() {
        assertParse("_", wildcard());
    }

    @Test
    public void intLiterals() {
        assertParse("42", literal(BigInteger.valueOf(42)));
    }

    @Test
    public void stringLiteral() {
        assertParse("\"foo\"", literal("foo"));
    }

    @Test
    public void unitPattern() {
        assertParse("()", constructor(UNIT));
    }

    @Test
    public void consPatterns() {
        assertParse("[]", constructor(NIL));
        assertParse("x:y", constructor(CONS, variable("x"), variable("y")));
        assertParse("x:y:z",
                constructor(CONS, variable("x"),
                        constructor(CONS, variable("y"),
                                variable("z"))));
    }

    @Test
    public void listPatterns() {
        assertParse("[x, y, z]",
                constructor(CONS, variable("x"),
                        constructor(CONS, variable("y"),
                                constructor(CONS, variable("z"),
                                        constructor(NIL)))));
    }

    @Test
    public void tuplePatterns() {
        assertParse("(x,y)", constructor("(,)", variable("x"), variable("y")));
        assertParse("(x,y,z)", constructor("(,,)", variable("x"), variable("y"), variable("z")));
        assertParse("(x,y,z,w)", constructor("(,,,)", variable("x"), variable("y"), variable("z"), variable("w")));
    }

    @Test
    public void constructorPattern() {
        assertParse("Foo", constructor("Foo"));
        assertParse("Foo x y", constructor("Foo", variable("x"), variable("y")));
        assertParse("Foo Bar Baz", constructor("Foo", constructor("Bar"), constructor("Baz")));
    }

    @Test
    public void complexPatterns() {
    }

    private static void assertParse(String source, Pattern expected) {
        PatternParser parser = new PatternParser(new Lexer(source));

        Pattern pattern = parser.parsePattern();
        assertThat(pattern, is(expected));
    }
}
