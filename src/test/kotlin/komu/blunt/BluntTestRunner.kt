package komu.blunt

import komu.blunt.eval.Evaluator
import komu.blunt.parser.Parser
import org.junit.Test

class BluntTestRunner {

    @Test
    fun smokeTests() {
        val evaluator = Evaluator()

        evaluator.loadResource("prelude.blunt")
        evaluator.loadResource("smoke-tests.blunt")

        evaluator.evaluate(Parser("runTests ()").parseExpression())
    }
}
