package komu.blunt.eval;

import komu.blunt.core.CoreExpression;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.parser.Parser;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EvaluatorTest {

    @Test
    public void selfEvaluatingObjects() {
        assertThatEvaluating("42", produces(42));
        assertThatEvaluating("true", produces(true));
        assertThatEvaluating("false", produces(false));
    }

    @Test
    public void primitiveOperators() {
        assertThatEvaluating("1 + 2", produces(3));
    }

    @Test
    public void ifExpression() {
        assertThatEvaluating("if true then 1 + 2 else 3 + 4", produces(3));
        assertThatEvaluating("if false then 1 + 2 else 3 + 4", produces(7));
    }

    @Test
    public void lambdaExpression() {
        assertThatEvaluating("fn x -> x", is(instanceOf(CompoundProcedure.class)));
        assertThatEvaluating("(fn x -> x + 1) 2", produces(3));
        assertThatEvaluating("(fn x -> fn y -> x + y) 3 4", produces(7));
    }

    @Test
    public void equality() {
        assertThatEvaluating("1 = 1", produces(true));
        assertThatEvaluating("1 = 2", produces(false));
    }

    @Test
    public void nestedCalls() {
        assertThatEvaluating("2 * 3 + ((5 + 6) * 7 * 8)", produces(622));
    }

    @Test
    public void polymorphicTypeWithDifferentInstantiations() {
        assertThatEvaluating("true = (1 = 1)", produces(true));
    }

    @Test
    public void equalityBetweenDifferentTypes() {
        assertStaticError("2 = \"foo\"");
    }

    @Test
    @Ignore
    public void tryingToDefineSameVariableMultipleTimes() {
        assertStaticError("(lambda (x x) 0)");
    }

    @Test
    public void accessingUnboundVariable() {
        assertStaticError("fn x -> y");
    }

    @Test
    public void typeErrors() {
        assertStaticError("if 0 then 1 else 2");
        assertStaticError("if true then 1 else false");
    }

    @Test
    public void typeInference() {
        assertThatEvaluating("fn n -> n", is(anything()));
        assertThatEvaluating("(fn n -> n) 42", produces(42));
    }

    @Test
    public void let() {
        assertThatEvaluating("let x = 42 in x", produces(42));
        // let x = 1; y = 2 in x + y
        assertThatEvaluating("let x = 42 in let y = 3 in x+y", produces(45));
    }

    @Test
    public void sequence() {
        assertThatEvaluating("1; 2; 3", produces(3));
    }
    
    @Test
    @Ignore
    public void setExpression() {
        assertThatEvaluating("(let ((x 1)) (begin (set! x 2) x))", produces(2));
    }

    @Test
    @Ignore
    public void letSequencing() {
        assertThatEvaluating("(let ((x 1)) (set! x 2) x)", produces(2));
    }

    @Test
    public void letRec() {
        assertThatEvaluating("let rec f = fn n -> if 0 = n then 1 else n * f (n - 1) in f 10)", produces(3628800));
    }

    @Test
    public void pairs() {
        assertThatEvaluating("fst (1, \"foo\")", produces(1));
        assertThatEvaluating("snd (1, \"foo\")", produces("foo"));
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

    private static CoreExpression analyze(String expr) {
        return new Evaluator().analyze(Parser.parse(expr));
    }

    private static Object evaluate(String expr) {
        return new Evaluator().evaluate(Parser.parse(expr));
    }

    private static Matcher<Object> produces(final int value) {
        return CoreMatchers.<Object>is(BigInteger.valueOf(value));
    }

    private static Matcher<Object> produces(final String value) {
        return CoreMatchers.<Object>is(value);
    }
    
    private static Matcher<Object> produces(final boolean value) {
        return CoreMatchers.<Object>is(value);
    }
}
