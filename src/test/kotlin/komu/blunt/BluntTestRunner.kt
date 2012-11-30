package komu.blunt

import komu.blunt.eval.Evaluator
import komu.blunt.parser.parseExpression
import org.junit.Test as test

class BluntTestRunner {

    test fun smokeTests() {
        val evaluator = Evaluator()

        evaluator.loadResource("prelude.blunt")
        evaluator.loadResource("smoke-tests.blunt")

        evaluator.evaluate(parseExpression("runTests ()"))
    }
}
