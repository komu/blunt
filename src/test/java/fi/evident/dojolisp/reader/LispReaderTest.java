package fi.evident.dojolisp.reader;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static fi.evident.dojolisp.types.Symbol.symbol;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LispReaderTest {

    @Test
    public void quoted() {
        assertThat(firstFormOf("'foo"), isObject(asList(symbol("quote"), symbol("foo"))));
    }

    private static Object firstFormOf(String input) {
        return LispReader.parse(input);
    }

    private static Matcher<Object> isObject(Object expected) {
        return is(expected);
    }
}
