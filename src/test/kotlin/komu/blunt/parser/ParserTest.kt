package komu.blunt.parser

import org.junit.Assert.assertThat
import org.junit.Test
import java.math.BigInteger

class ParserTest {

    @Test fun numberLiterals() {
        assertThat(parsing("42"), producesConstant(BigInteger.valueOf(42)))
    }

    @Test fun stringLiterals() {
        assertThat(parsing("\"foo\""), producesConstant("foo"))
    }
    
    @Test fun variables() {
        //assertThat(parsing("foo"), producesVariable("foo"))
    }
    
    @Test fun ifIsRewrittenToCase() {
        assertThat(parsing("if true then 1 else 2"), producesExpressionMatching("case true of [True -> 1, False -> 2]"))
    }

    @Test fun letExpression() {
        assertThat(parsing("let x = 1 in y"), producesExpressionMatching("(let ([x 1]) y)"))
    }

    @Test fun letRecExpression() {
        assertThat(parsing("let rec x = z in y"), producesExpressionMatching("(letrec ([x z]) y)"))
    }
    
    @Test fun lambdaExpression() {
        assertThat(parsing("\\x -> y"), producesExpressionMatching("(\\ x -> y)"))
    }
    
    @Test fun parenthesizedExpression() {
        assertThat(parsing("(42)"), producesConstant(BigInteger.valueOf(42)))
        assertThat(parsing("(((43)))"), producesConstant(BigInteger.valueOf(43)))
    }

    @Test fun binaryOperators() {
        assertThat(parsing("a == b"), producesExpressionMatching("((== a) b)"))
        assertThat(parsing("a + b"), producesExpressionMatching("((+ a) b)"))
        assertThat(parsing("a - b"), producesExpressionMatching("((- a) b)"))
    }

    @Test fun binaryOperatorAssociativity() {
        assertThat(parsing("a - b + c - d + e"), producesExpressionMatching("((+ ((- ((+ ((- a) b)) c)) d)) e)"))
    }
    
    @Test fun operatorPrecedence() {
        assertThat(parsing("a + b * c - d"), producesExpressionMatching("((- ((+ a) ((* b) c))) d)"))
    }

    @Test fun sequences() {
        assertThat(parsing("a; b"), producesExpressionMatching("(begin a b)"))
        assertThat(parsing("a; b; c"), producesExpressionMatching("(begin (begin a b) c)"))
    }
    
    @Test fun application() {
        assertThat(parsing("a b"), producesExpressionMatching("(a b)"));
        assertThat(parsing("a b c"), producesExpressionMatching("((a b) c)"));
        assertThat(parsing("a (b c)"), producesExpressionMatching("(a (b c))"));
    }

    @Test fun tuples() {
        assertThat(parsing("(a, b)"), producesExpressionMatching("(((,) a) b)"));
        assertThat(parsing("(a+b, c*d)"), producesExpressionMatching("(((,) ((+ a) b)) ((* c) d))"));
    }

    private fun parsing(s: String) =
        Parser(s).parseExpression()
}
