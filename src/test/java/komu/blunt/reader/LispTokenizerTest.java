package komu.blunt.reader;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static komu.blunt.objects.Symbol.symbol;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LispTokenizerTest {

    @Test
    public void emptyStreamReturnsEof() {
        assertThat(firstTokenOf(""), isToken(Token.EOF));
    }

    @Test
    public void integer() {
        assertThat(firstTokenOf("42"), isToken(42));
    }

    @Test
    public void string() {
        assertThat(firstTokenOf("\"foo\""), isToken("foo"));
    }

    @Test
    public void symbols() {
        assertThat(firstTokenOf("foo"),         isToken(symbol("foo")));
        assertThat(firstTokenOf("foo-bar baz"), isToken(symbol("foo-bar")));
    }
    
    @Test
    public void tokens() {
        assertThat(firstTokenOf("'"), isToken(Token.QUOTE));
        assertThat(firstTokenOf("("), isToken(Token.LPAREN));
        assertThat(firstTokenOf(")"), isToken(Token.RPAREN));
    }

    @Test
    public void whitespaceIsSkipped() {
        assertThat(firstTokenOf("  '"),    isToken(Token.QUOTE));
        assertThat(firstTokenOf("\t \n("), isToken(Token.LPAREN));
        assertThat(firstTokenOf("\n\n)"),  isToken(Token.RPAREN));
        assertThat(firstTokenOf("\nfoo"),  isToken(symbol("foo")));
        assertThat(firstTokenOf("  42"),   isToken(42));
    }
    
    public Matcher<Object> isToken(Object token) {
        return is(token);
    }

    private static Object firstTokenOf(String s) {
        try {
            return new LispTokenizer(new StringReader(s)).readToken();
        } catch (IOException e) {
            throw new RuntimeException("unexpected IOException: " + e, e);
        }
    }
}
