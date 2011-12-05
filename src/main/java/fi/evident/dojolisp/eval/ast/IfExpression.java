package fi.evident.dojolisp.eval.ast;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.types.Type;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class IfExpression extends Expression {

    private final Expression condition;
    private final Expression consequent;
    private final Expression alternative;

    public IfExpression(Expression condition, Expression consequent, Expression alternative) {
        this.condition = requireNonNull(condition);
        this.consequent = requireNonNull(consequent);
        this.alternative = requireNonNull(alternative);
    }

    @Override
    public Object evaluate(Environment env) {
        Object result = condition.evaluate(env);
        if (!Boolean.FALSE.equals(result))
            return consequent.evaluate(env);
        else
            return alternative.evaluate(env);
    }

    @Override
    public Type typeCheck() {
        Type conditionType = condition.typeCheck();
        
        conditionType.unify(Type.BOOLEAN);

        Type consequentType = consequent.typeCheck();
        Type alternativeType = alternative.typeCheck();

        return consequentType.unify(alternativeType);
    }
}
