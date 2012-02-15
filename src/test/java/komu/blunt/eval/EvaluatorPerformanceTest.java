package komu.blunt.eval;

import komu.blunt.parser.Parser;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.String.format;
import static org.junit.Assert.fail;

public class EvaluatorPerformanceTest {

    private static final Evaluator evaluator = new Evaluator();

    @Rule
    public final Profiler profiler = new Profiler();
    
    @BeforeClass
    public static void loadPrelude() throws IOException {
        evaluator.loadResource("prelude.blunt");
    }
    
    @Test
    @MaxSteps(2_021_216)
    public void sorting() {
        evaluate("sort (range 0 500)");
    }

    private static Object evaluate(String expr) {
        return evaluator.evaluate(Parser.$classobj.parseExpression(expr));
    }

    private static class Profiler extends TestWatcher {

        private long startTime;
        private long startSteps;
        
        @Override
        protected void starting(Description description) {
            startTime = System.currentTimeMillis();
            startSteps = evaluator.getSteps();
        }

        @Override
        protected void finished(Description description) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            long elapsedSteps = evaluator.getSteps() - startSteps;

            System.out.printf("%s: %,d steps in %dms\n", description.getMethodName(), elapsedSteps, elapsedTime);

            MaxSteps maxSteps = description.getAnnotation(MaxSteps.class);
            if (maxSteps != null && elapsedSteps > maxSteps.value())
                fail(format("expected maximum %,d steps, but got %,d", maxSteps.value(), elapsedSteps));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface MaxSteps {
        long value();
    }
}
