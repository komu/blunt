package komu.blunt.reader;

import org.hamcrest.Matcher;
import org.junit.Test;

import static komu.blunt.objects.Symbol.symbol;
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
