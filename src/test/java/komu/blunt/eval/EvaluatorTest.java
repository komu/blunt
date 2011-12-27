package komu.blunt.eval;

import komu.blunt.analyzer.AnalyzationException;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.parser.Parser;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class EvaluatorTest {

    private static final Evaluator evaluator = new Evaluator();

    @BeforeClass
    public static void loadPrelude() throws IOException {
        evaluator.loadResource("prelude.blunt");
    }

    @Test
    public void selfEvaluatingObjects() {
        assertThatEvaluating("42", produces(42));
        assertThatEvaluating("True", produces(true));
        assertThatEvaluating("False", produces(false));
    }

    @Test
    public void primitiveOperators() {
        assertThatEvaluating("1 + 2", produces(3));
    }

    @Test
    public void ifExpression() {
        assertThatEvaluating("if True then 1 + 2 else 3 + 4", produces(3));
        assertThatEvaluating("if False then 1 + 2 else 3 + 4", produces(7));
    }

    @Test
    public void lambdaExpression() {
        assertThatEvaluating("\\ x -> x", is(instanceOf(CompoundProcedure.class)));
        assertThatEvaluating("(\\ x -> x + 1) 2", produces(3));
        assertThatEvaluating("(\\ x -> \\ y -> x + y) 3 4", produces(7));
        assertThatEvaluating("(\\ x y -> x + y) 3 4", produces(7));
    }

    @Test
    public void equality() {
        assertThatEvaluating("1 == 1", produces(true));
        assertThatEvaluating("1 == 2", produces(false));
    }

    @Test
    public void nestedCalls() {
        assertThatEvaluating("2 * 3 + ((5 + 6) * 7 * 8)", produces(622));
    }

    @Test
    public void polymorphicTypeWithDifferentInstantiations() {
        assertThatEvaluating("True == (1 == 1)", produces(true));
    }

    @Test
    public void equalityBetweenDifferentTypes() {
        assertStaticError("2 == \"foo\"");
    }

    @Test
    public void accessingUnboundVariable() {
        assertStaticError("\\x -> y");
    }

    @Test
    public void typeErrors() {
        assertStaticError("if 0 then 1 else 2");
        assertStaticError("if true then 1 else false");
    }

    @Test
    public void typeInference() {
        assertThatEvaluating("\\n -> n", is(anything()));
        assertThatEvaluating("(\\n -> n) 42", produces(42));
    }

    @Test
    public void let() {
        assertThatEvaluating("let x = 42 in x", produces(42));
        //assertThatEvaluating("let x = 42; y = 2 in x + y", produces(3));
        assertThatEvaluating("let x = 42 in let y = 3 in x+y", produces(45));
    }

    @Test
    public void sequence() {
        assertThatEvaluating("1; 2; 3", produces(3));
    }
    
    @Test
    public void letRec() {
        assertThatEvaluating("let rec f = \\n -> if 0 == n then 1 else n * f (n - 1) in f 10)", produces(3628800));
        assertThatEvaluating("let rec f n = if 0 == n then 1 else n * f (n - 1) in f 10)", produces(3628800));
    }

    @Test
    public void letFunctions() {
        assertThatEvaluating("let f x = x * x in f 4", produces(16));
    }

    @Test
    public void pairs() {
        assertThatEvaluating("fst (1, \"foo\")", produces(1));
        assertThatEvaluating("snd (1, \"foo\")", produces("foo"));
    }

    @Test
    public void lists() {
        assertThatEvaluating("length [1,2,3]", produces(3));
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
        return evaluator.evaluate(Parser.parse(expr));
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
