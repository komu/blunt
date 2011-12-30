package komu.blunt.parser;

import komu.blunt.types.Type;
import org.junit.Assert;
import org.junit.Test;

import static komu.blunt.types.Type.*;
import static org.hamcrest.CoreMatchers.is;

public class TypeParserTest {

    @Test
    public void typeVariables() {
        verifyParsing("a", typeVariable("a"));
        verifyParsing("foo", typeVariable("foo"));
        verifyParsing("(a)", typeVariable("a"));
    }

    @Test
    public void functionTypes() {
        verifyParsing("a -> b", functionType(typeVariable("a"), typeVariable("b")));
        verifyParsing("a -> b -> c", functionType(typeVariable("a"), functionType(typeVariable("b"), typeVariable("c"))));
        verifyParsing("a -> (b -> c)", functionType(typeVariable("a"), functionType(typeVariable("b"), typeVariable("c"))));
        verifyParsing("(a -> b) -> c", functionType(functionType(typeVariable("a"), typeVariable("b")), typeVariable("c")));
    }
    
    @Test
    public void unitType()  {
        verifyParsing("()", Type.UNIT);
    }

    @Test
    public void tupleTypes()  {
        verifyParsing("(a, b, c)", tupleType(typeVariable("a"), typeVariable("b"), typeVariable("c")));
    }

    @Test
    public void primitiveTypes() {
        verifyParsing("Foo", genericType("Foo"));
    }

    @Test
    public void genericTypes() {
        verifyParsing("Foo a", genericType("Foo", typeVariable("a")));
        verifyParsing("Foo Bar a", genericType("Foo", genericType("Bar"), typeVariable("a")));
        verifyParsing("Foo (Bar a)", genericType("Foo", genericType("Bar", typeVariable("a"))));
    }

    @Test
    public void listTypes() {
        verifyParsing("[a]", listType(typeVariable("a")));
    }

    private static void verifyParsing(String s, Type type) {
        Assert.assertThat(parse(s), is(type));
    }

    private static Type parse(String s) {
        return new TypeParser(new Lexer(s)).parseType();
    }
}
