package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.ast.Expression;
import fi.evident.dojolisp.eval.types.Type;
import fi.evident.dojolisp.stdlib.BasicFunctions;

import java.util.ArrayList;
import java.util.List;

public final class Evaluator {

    private final Analyzer analyzer = new Analyzer();
    private final StaticEnvironment rootStaticEnvironment = new StaticEnvironment();
    private final Environment rootRuntimeEnvironment;

    public Evaluator() {
        List<StaticBinding> bindings = new ArrayList<StaticBinding>();
        
        bindings.add(new StaticBinding("true", Type.BOOLEAN, true));
        bindings.add(new StaticBinding("false", Type.BOOLEAN, false));

        BasicFunctions.register(bindings);

        rootRuntimeEnvironment = new Environment(bindings.size());

        for (StaticBinding binding : bindings) {
            VariableReference ref = rootStaticEnvironment.define(binding.name, binding.type);
            rootRuntimeEnvironment.set(ref, binding.value);
        }
    }

    public Expression analyze(Object form) {
        Expression exp = analyzer.analyze(form, rootStaticEnvironment);
        exp.typeCheck();
        return exp;
    }

    public Object evaluate(Object form) {
        Expression expression = analyze(form);
        return expression.evaluate(rootRuntimeEnvironment);
    }
    
    public ResultWithType evaluateWithType(Object form) {
        Expression expression = analyzer.analyze(form, rootStaticEnvironment);
        Type type = expression.typeCheck();
        Object result = expression.evaluate(rootRuntimeEnvironment);
        return new ResultWithType(result, type);
    }
}
