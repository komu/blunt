package komu.blunt.parser;

import komu.blunt.ast.ASTExpression;
import org.hamcrest.Matcher;
import org.junit.Test;

import static komu.blunt.parser.ASTMatchers.producesConstant;
import static komu.blunt.parser.ASTMatchers.producesVariable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ParserTest {
    
    @Test
    public void emptyInput() {
        assertThat(parsing(""),      producesEof());
        assertThat(parsing("  "),    producesEof());
        assertThat(parsing("\n "),   producesEof());
        assertThat(parsing(" ; \n"), producesEof());
        assertThat(parsing(" ; "),   producesEof());
    }

    @Test
    public void numberLiterals() {
        assertThat(parsing("42"), producesConstant(42));
    }

    @Test
    public void stringLiterals() {
        assertThat(parsing("\"foo\""), producesConstant("foo"));
    }
    
    @Test
    public void variables() {
        assertThat(parsing("foo"), producesVariable("foo"));
    }

    private static ASTExpression parsing(String s) {
        return Parser.parse(s);
    }

    private static Matcher<Object> producesEof() {
        return is(nullValue());
    }
}
