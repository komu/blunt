package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.reader.LispReader;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import static fi.evident.dojolisp.types.Symbol.symbol;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;

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

    private static void assertThatEvaluating(String expr, Matcher<Object> matcher) {
        assertThat(new Evaluator().evaluate(LispReader.parse(expr)), matcher);
    }

    private static Matcher<Object> produces(final Object value) {
        return CoreMatchers.is(value);
    }
}
