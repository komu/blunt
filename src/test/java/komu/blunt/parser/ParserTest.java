package komu.blunt.parser;

import komu.blunt.ast.ASTExpression;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.math.BigInteger;

import static komu.blunt.parser.ASTMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ParserTest {
    
    @Test
    public void numberLiterals() {
        assertThat(parsing("42"), producesConstant(BigInteger.valueOf(42)));
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
        assertThat(parsing("\\x -> y"), producesExpressionMatching("(lambda [x] y)"));
    }
    
    @Test
    public void parenthesizedExpression() {
        assertThat(parsing("(42)"), producesConstant(BigInteger.valueOf(42)));
        assertThat(parsing("(((43)))"), producesConstant(BigInteger.valueOf(43)));
    }

    @Test
    public void binaryOperators() {
        assertThat(parsing("a == b"), producesExpressionMatching("((== a) b)"));
        assertThat(parsing("a + b"), producesExpressionMatching("((+ a) b)"));
        assertThat(parsing("a - b"), producesExpressionMatching("((- a) b)"));
    }

    @Test
    public void binaryOperatorAssociativity() {
        assertThat(parsing("a - b + c - d + e"), producesExpressionMatching("((+ ((- ((+ ((- a) b)) c)) d)) e)"));
    }
    
    @Test
    public void operatorPrecedence() {
        assertThat(parsing("a + b * c - d"), producesExpressionMatching("((- ((+ a) ((* b) c))) d)"));
    }

    @Test
    public void sequences() {
        assertThat(parsing("a; b"), producesExpressionMatching("(begin a b)"));
        assertThat(parsing("a; b; c"), producesExpressionMatching("(begin (begin a b) c)"));
    }
    
    @Test
    public void application() {
        assertThat(parsing("a b"), producesExpressionMatching("(a b)"));
        assertThat(parsing("a b c"), producesExpressionMatching("((a b) c)"));
        assertThat(parsing("a (b c)"), producesExpressionMatching("(a (b c))"));
    }

    @Test
    public void tuples() {
        assertThat(parsing("(a, b)"), producesExpressionMatching("(tuple a b)"));
        assertThat(parsing("(a+b, c*d)"), producesExpressionMatching("(tuple ((+ a) b) ((* c) d))"));
    }

    private static ASTExpression parsing(String s) {
        return Parser.parseExpression(s);
    }

    private static Matcher<Object> producesEof() {
        return is(nullValue());
    }
}
