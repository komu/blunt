package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.ast.Expression;
import fi.evident.dojolisp.reader.LispReader;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import static fi.evident.dojolisp.types.Symbol.symbol;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EvaluatorTest {

    @Test
    public void selfEvaluatingObjects() {
        assertThatEvaluating("42", produces(42));
        assertThatEvaluating("true", produces(true));
        assertThatEvaluating("false", produces(false));
        assertThatEvaluating("null", produces(null));
    }

    @Test
    public void quoted() {
        assertThatEvaluating("'foo", produces(symbol("foo")));
        assertThatEvaluating("'(1 2 3)", produces(asList(1, 2, 3)));
    }
    
    @Test
    public void primitiveApplication() {
        assertThatEvaluating("(+ 1 2)", produces(3));
    }

    @Test
    public void ifExpression() {
        assertThatEvaluating("(if true (+ 1 2) (+ 3 4))", produces(3));
        assertThatEvaluating("(if false (+ 1 2) (+ 3 4))", produces(7));
    }

    @Test
    public void lambdaExpression() {
        assertThatEvaluating("((lambda (x) (+ x 1)) 2)", produces(3));
        assertThatEvaluating("(((lambda (x) (lambda (y) (+ x y))) 3) 4)", produces(7));
    }

    @Test
    public void equality() {
        assertThatEvaluating("(= 1)", produces(true));
        assertThatEvaluating("(= 1 1)", produces(true));
        assertThatEvaluating("(= 1 2)", produces(false));
        assertThatEvaluating("(= 1 1 1)", produces(true));
        assertThatEvaluating("(= 1 2 1)", produces(false));
        assertThatEvaluating("(= 1 1 2)", produces(false));
        assertThatEvaluating("(= 1 2 2)", produces(false));
    }

    @Test
    public void staticErrors() {
        assertStaticError("(lambda (x x) 0)");
        assertStaticError("(lambda (x) y)");
    }

    private void assertStaticError(String expr) {
        try {
            analyze(expr);
            fail("Expected error when analyzing: " + expr);
        }  catch (AnalyzationException e) {
        }
    }

    private static void assertThatEvaluating(String expr, Matcher<Object> matcher) {
        assertThat(evaluate(expr), matcher);
    }

    private static Expression analyze(String expr) {
        return new Evaluator().analyze(LispReader.parse(expr));
    }

    private static Object evaluate(String expr) {
        return new Evaluator().evaluate(LispReader.parse(expr));
    }

    private static Matcher<Object> produces(final Object value) {
        return CoreMatchers.is(value);
    }
}
