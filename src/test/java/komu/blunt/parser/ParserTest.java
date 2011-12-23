package komu.blunt.parser;

import komu.blunt.ast.ASTExpression;
import org.hamcrest.Matcher;
import org.junit.Test;

import static komu.blunt.parser.ASTMatchers.*;
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
    
    @Test
    public void ifExpression() {
        assertThat(parsing("if true then 1 else 2"), producesExpressionMatching("(if true 1 2)"));
    }

    @Test
    public void letExpression() {
        assertThat(parsing("let x = 1 in y"), producesExpressionMatching("(let ([x 1]) y)"));
    }

    @Test
    public void letRecExpression() {
        assertThat(parsing("let rec x = z in y"), producesExpressionMatching("(letrec ([x z]) y)"));
    }
    
    @Test
    public void lambdaExpression() {
        assertThat(parsing("fn x -> y"), producesExpressionMatching("(lambda [x] y)"));
    }
    
    @Test
    public void parenthesizedExpression() {
        assertThat(parsing("(42)"), producesConstant(42));
        assertThat(parsing("(((43)))"), producesConstant(43));
    }

    private static ASTExpression parsing(String s) {
        return Parser.parse(s);
    }

    private static Matcher<Object> producesEof() {
        return is(nullValue());
    }
}
