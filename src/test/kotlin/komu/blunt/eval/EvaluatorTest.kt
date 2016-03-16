package komu.blunt.eval;

import komu.blunt.analyzer.AnalyzationException
import komu.blunt.core.CoreExpression
import komu.blunt.objects.CompoundProcedure
import komu.blunt.parser.Parser
import komu.blunt.stdlib.booleanToConstructor
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.Matcher
import org.junit.Assert.assertThat
import org.junit.Test
import java.math.BigInteger
import kotlin.test.fail
import org.hamcrest.CoreMatchers.`is` as isEqualTo
import org.junit.BeforeClass as beforeclass

class EvaluatorTest {

    companion object {
        val evaluator = Evaluator().apply { loadResource("prelude.blunt") }
    }

    @Test fun selfEvaluatingObjects() {
        assertThatEvaluating("42", produces(42))
        assertThatEvaluating("True", produces(true))
        assertThatEvaluating("False", produces(false))
    }

    @Test fun primitiveOperators() {
        assertThatEvaluating("1 + 2", produces(3))
    }

    @Test fun patternsInLambdas() {
        assertThatEvaluating("(\\(a,b) -> a) (42, 24)", produces(42))
    }

    @Test fun ifExpression() {
        assertThatEvaluating("if True then 1 + 2 else 3 + 4", produces(3))
        assertThatEvaluating("if False then 1 + 2 else 3 + 4", produces(7))
    }

    @Test fun lambdaExpression() {
        assertThatEvaluating("\\ x -> x", instanceOf<Any?>(CompoundProcedure::class.java))
        assertThatEvaluating("(\\ x -> x + 1) 2", produces(3))
        assertThatEvaluating("(\\ x -> \\ y -> x + y) 3 4", produces(7))
        assertThatEvaluating("(\\ x y -> x + y) 3 4", produces(7))
    }

    @Test fun equality() {
        assertThatEvaluating("1 == 1", produces(true))
        assertThatEvaluating("1 == 2", produces(false))
    }

    @Test fun nestedCalls() {
        assertThatEvaluating("2 * 3 + ((5 + 6) * 7 * 8)", produces(622))
    }

    @Test fun polymorphicTypeWithDifferentInstantiations() {
        assertThatEvaluating("True == (1 == 1)", produces(true))
    }

    @Test fun equalityBetweenDifferentTypes() {
        assertStaticError("2 == \"foo\"")
    }

    @Test fun accessingUnboundVariable() {
        assertStaticError("\\x -> y")
    }

    @Test fun typeErrors() {
        assertStaticError("if 0 then 1 else 2");
        assertStaticError("if true then 1 else false");
    }

    @Test fun typeInference() {
        assertThatEvaluating("\\n -> n", isEqualTo(anything()))
        assertThatEvaluating("(\\n -> n) 42", produces(42));
    }

    @Test fun let() {
        assertThatEvaluating("let x = 42 in x", produces(42));
        //assertThatEvaluating("let x = 42; y = 2 in x + y", produces(3));
        assertThatEvaluating("let x = 42 in let y = 3 in x+y", produces(45));
    }

    @Test fun sequence() {
        assertThatEvaluating("1; 2; 3", produces(3));
    }

    @Test fun letRec() {
        assertThatEvaluating("let rec f = \\n -> if 0 == n then 1 else n * f (n - 1) in f 10)", produces(3628800));
        assertThatEvaluating("let rec f n = if 0 == n then 1 else n * f (n - 1) in f 10)", produces(3628800));
    }

    @Test fun letFunctions() {
        assertThatEvaluating("let f x = x * x in f 4", produces(16));
    }

    @Test fun pairs() {
        assertThatEvaluating("fst (1, \"foo\")", produces(1));
        assertThatEvaluating("snd (1, \"foo\")", produces("foo"));
    }

    @Test fun lists() {
        assertThatEvaluating("length [1,2,3]", produces(3));
    }

    @Test fun simpleCase() {
        assertThatEvaluating("case 4 of\n  n -> n\n", produces(4));
    }

    @Test fun matchingUnit() {
        assertThatEvaluating("case () of\n  () -> 2\n", produces(2));
    }

    @Test fun constructorMatchingCase() {
        assertThatEvaluating("case Nothing of\n  Just x -> 1\n  Nothing -> 2\n", produces(2));
        assertThatEvaluating("case Just 3 of\n  Just x -> 1\n  Nothing -> 2\n", produces(1));
        assertThatEvaluating("case Just 3 of\n  Just x -> x\n  Nothing -> 2\n", produces(3));
    }

    @Test fun listMatching() {
        assertThatEvaluating("case [] of\n  [] -> 42\n", produces(42));
        assertThatEvaluating("case [1] of\n  x:xs -> x\n", produces(1));
        assertThatEvaluating("case [1,2,3,4,5,6,7] of\n  x:_:y:_ -> x+y\n", produces(4));
    }

    @Test fun matchingTuples() {
        assertThatEvaluating("case (1,2) of\n  (x,y) -> x+y\n", produces(3));
        assertThatEvaluating("case (1,2,\"foo\") of\n  (x,y,z) -> z\n", produces("foo"));
    }

    fun assertStaticError(expr: String) {
        try {
            analyze(expr)
            fail("Expected error when analyzing '$expr'")
        }  catch (e: AnalyzationException) {
        }
    }

    fun assertThatEvaluating(expr: String, matcher: Matcher<Any?>) {
        assertThat(evaluate(expr), matcher)
    }

    private fun analyze(expr: String): CoreExpression =
        Evaluator().analyze(parseExpression(expr))

    private fun evaluate(expr: String): Any? =
        evaluator.evaluate(parseExpression(expr))

    private fun parseExpression(expr: String) =
        Parser(expr).parseExpression()

    private fun produces(value: Long): Matcher<Any?> =
        isEqualTo<Any?>(BigInteger.valueOf(value))

    private fun produces(value: String): Matcher<Any?> =
        isEqualTo<Any?>(value)

    private fun produces(value: Boolean): Matcher<Any?> =
        isEqualTo<Any?>(booleanToConstructor(value))
}
