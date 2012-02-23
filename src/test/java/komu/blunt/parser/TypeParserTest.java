package komu.blunt.parser;

import komu.blunt.types.Predicate;
import komu.blunt.types.Qualified;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static komu.blunt.types.Type.*;
import static org.hamcrest.CoreMatchers.is;

public class TypeParserTest {
/*
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

    @Test
    public void parseQualified() {
        TypeVariable a = typeVariable("a");
        TypeVariable b = typeVariable("b");

        verifyParsing("Num a => a -> a", new Qualified<>(Arrays.<Predicate>asList(isIn("Num", a)), functionType(a, a)));
        verifyParsing("(Num a) => a -> a", new Qualified<>(Arrays.<Predicate>asList(isIn("Num", a)), functionType(a, a)));
        verifyParsing("(Num a, Ord a, Num b) => a -> b",
                new Qualified<>(Arrays.<Predicate>asList(isIn("Num", a), isIn("Ord", a), isIn("Num", b)),
                        functionType(a, b)));
    }
    
    @Test
    public void parseQualifiedWithoutQualifications() {
        verifyParsing("a -> a", new Qualified<>(functionType(typeVariable("a"), typeVariable("a"))));
    }

    private static void verifyParsing(String s, Qualified<Type> type) {
        Assert.assertThat(TypeParser.$classobj.parseQualified(s), is(type));
    }

    private static void verifyParsing(String s, Type type) {
        Assert.assertThat(TypeParser.$classobj.parseType(s), is(type));
    }
    */
}
