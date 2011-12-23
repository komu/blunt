package komu.blunt.eval;

import komu.blunt.core.CoreExpression;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.parser.Parser;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;
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
    @Ignore
    public void quoted() {
        assertThatEvaluating("'foo", produces(symbol("foo")));
        assertThatEvaluating("'(1 2 3)", produces(asList(1, 2, 3)));
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
        //assertThatEvaluating("((lambda (x) (+ x 1)) 2)", produces(3));
        //assertThatEvaluating("(((lambda (x) (lambda (y) (+ x y))) 3) 4)", produces(7));
    }

    @Test
    public void equality() {
        assertThatEvaluating("1 = 1", produces(true));
        assertThatEvaluating("1 = 2", produces(false));
    }

    @Test
    @Ignore
    public void nestedCalls() {
        assertThatEvaluating("(+ (* 2 3) (* (+ 5 6) (* 7 8)))", produces(622));
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
    public void varargsInvocation() {
        assertThatEvaluating("(+)", produces(0));
        assertThatEvaluating("(+ 2)", produces(2));
        assertThatEvaluating("(+ 2 3 4)", produces(9));
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
        //assertThatEvaluating("((lambda (n) n) 42)", produces(42));
    }

    @Test
    public void let() {
        assertThatEvaluating("let x = 42 in x", produces(42));
        //assertThatEvaluating("(let ((x 1) (y 2)) (+ x y))", produces(3));
        // let x = 1; y = 2 in x + y
        // let x = 1 in let y = 2 in x + y
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
    @Ignore
    public void letRec() {
        assertThatEvaluating("let rec f = fn n -> if (0 = n) then 1 else (f (n - 1)) in f 10)", produces(3628800));
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

    private static Matcher<Object> produces(final Object value) {
        return is(value);
    }
}
