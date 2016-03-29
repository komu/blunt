package komu.blunt.eval;

import komu.blunt.parser.parseExpression
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.fail

class EvaluatorPerformanceTest {

    val evaluator = Evaluator()

    @Before
    fun loadPrelude() {
        evaluator.loadResource("prelude.blunt");
    }
    
    @Test
    fun sorting() {
        evaluate("sort (range 0 500)", maxSteps = 2021216)
    }

    fun evaluate(expr: String, maxSteps: Long) {
        val startSteps = evaluator.steps
        val total = measureTimeMillis {
            evaluator.evaluate(parseExpression(expr))
        }
        val steps = evaluator.steps - startSteps

        print("evaluation of '$expr' took $steps steps in ${total}ms")

        if (steps > maxSteps)
            fail("expected maximum $maxSteps steps, but got $steps")
    }
}
