package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.ast.Expression;
import fi.evident.dojolisp.stdlib.BasicFunctions;

public final class Evaluator {

    private final Analyzer analyzer = new Analyzer();
    private final StaticEnvironment rootStaticEnvironment = new StaticEnvironment();
    private final Environment rootRuntimeEnvironment = new Environment();

    public Evaluator() {
        define("null", null);
        define("true", true);
        define("false", false);

        BasicFunctions.register(rootStaticEnvironment, rootRuntimeEnvironment);
    }
    
    public void define(String name, Object value) {
        rootRuntimeEnvironment.define(name, value);
        rootStaticEnvironment.define(name);
    }
    
    public Expression analyze(Object form) {
        return analyzer.analyze(form, rootStaticEnvironment);
    }

    public Object evaluate(Object form) {
        Expression expression = analyze(form);
        return expression.evaluate(rootRuntimeEnvironment);
    }
}
