package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.stdlib.BasicFunctions;

public final class Evaluator {

    private final Analyzer analyzer = new Analyzer();
    private final Environment rootEnvironment = new Environment();

    public Evaluator() {
        rootEnvironment.define("null", null);
        rootEnvironment.define("true", true);
        rootEnvironment.define("false", false);

        BasicFunctions.register(rootEnvironment);
    }
    
    public Object evaluate(Object form) {
        return analyzer.analyze(form).evaluate(rootEnvironment);
    }
}
