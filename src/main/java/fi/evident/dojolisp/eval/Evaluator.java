package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.ast.Expression;
import fi.evident.dojolisp.stdlib.BasicFunctions;

import java.util.ArrayList;
import java.util.List;

public final class Evaluator {

    private final Analyzer analyzer = new Analyzer();
    private final StaticEnvironment rootStaticEnvironment = new StaticEnvironment();
    private final Environment rootRuntimeEnvironment;

    public Evaluator() {
        List<StaticBinding> bindings = new ArrayList<StaticBinding>();
        
        bindings.add(new StaticBinding("null", null));
        bindings.add(new StaticBinding("true", true));
        bindings.add(new StaticBinding("false", false));

        BasicFunctions.register(bindings);

        rootRuntimeEnvironment = new Environment(bindings.size());

        for (StaticBinding binding : bindings) {
            VariableReference ref = rootStaticEnvironment.define(binding.name);
            rootRuntimeEnvironment.set(ref, binding.value);
        }
    }

    public Expression analyze(Object form) {
        return analyzer.analyze(form, rootStaticEnvironment);
    }

    public Object evaluate(Object form) {
        Expression expression = analyze(form);
        return expression.evaluate(rootRuntimeEnvironment);
    }
}
