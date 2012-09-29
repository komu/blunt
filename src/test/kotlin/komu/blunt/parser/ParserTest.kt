package komu.blunt.parser

import java.math.BigInteger
import org.junit.Assert.assertThat
import org.junit.Test as test

public class ParserTest {

    test fun numberLiterals() {
        assertThat(parsing("42"), producesConstant(BigInteger.valueOf(42)))
    }

    test fun stringLiterals() {
        assertThat(parsing("\"foo\""), producesConstant("foo"))
    }
    
    test fun variables() {
        //assertThat(parsing("foo"), producesVariable("foo"))
    }
    
    test fun ifIsRewrittenToCase() {
        assertThat(parsing("if true then 1 else 2"), producesExpressionMatching("case true of [True -> 1, False -> 2]"))
    }

    test fun letExpression() {
        assertThat(parsing("let x = 1 in y"), producesExpressionMatching("(let ([x 1]) y)"))
    }

    test fun letRecExpression() {
        assertThat(parsing("let rec x = z in y"), producesExpressionMatching("(letrec ([x z]) y)"))
    }
    
    test fun lambdaExpression() {
        assertThat(parsing("\\x -> y"), producesExpressionMatching("(lambda x y)"))
    }
    
    test fun parenthesizedExpression() {
        assertThat(parsing("(42)"), producesConstant(BigInteger.valueOf(42)))
        assertThat(parsing("(((43)))"), producesConstant(BigInteger.valueOf(43)))
    }

    test fun binaryOperators() {
        assertThat(parsing("a == b"), producesExpressionMatching("((== a) b)"))
        assertThat(parsing("a + b"), producesExpressionMatching("((+ a) b)"))
        assertThat(parsing("a - b"), producesExpressionMatching("((- a) b)"))
    }

    test fun binaryOperatorAssociativity() {
        assertThat(parsing("a - b + c - d + e"), producesExpressionMatching("((+ ((- ((+ ((- a) b)) c)) d)) e)"))
    }
    
    test fun operatorPrecedence() {
        assertThat(parsing("a + b * c - d"), producesExpressionMatching("((- ((+ a) ((* b) c))) d)"))
    }

    test fun sequences() {
        assertThat(parsing("a; b"), producesExpressionMatching("(begin a b)"))
        assertThat(parsing("a; b; c"), producesExpressionMatching("(begin (begin a b) c)"))
    }
    
    test fun application() {
        assertThat(parsing("a b"), producesExpressionMatching("(a b)"));
        assertThat(parsing("a b c"), producesExpressionMatching("((a b) c)"));
        assertThat(parsing("a (b c)"), producesExpressionMatching("(a (b c))"));
    }

    test fun tuples() {
        assertThat(parsing("(a, b)"), producesExpressionMatching("(((,) a) b)"));
        assertThat(parsing("(a+b, c*d)"), producesExpressionMatching("(((,) ((+ a) b)) ((* c) d))"));
    }

    private fun parsing(s: String) =
        parseExpression(s)
}
