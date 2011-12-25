package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreIfExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTIf extends ASTExpression {
    public final ASTExpression test;
    public final ASTExpression consequent;
    public final ASTExpression alternative;

    public ASTIf(ASTExpression test, ASTExpression consequent, ASTExpression alternative) {
        this.test = checkNotNull(test);
        this.consequent = checkNotNull(consequent);
        this.alternative = checkNotNull(alternative);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreIfExpression(test.analyze(env, rootBindings), consequent.analyze(env, rootBindings), alternative.analyze(env, rootBindings));
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        Type conditionType = test.typeCheck(env);

        env.unify(Type.BOOLEAN, conditionType);

        Type consequentType = consequent.typeCheck(env);
        Type alternativeType = alternative.typeCheck(env);

        env.unify(consequentType, alternativeType);

        return consequentType;
    }

    @Override
    public String toString() {
        return "(if " + test + " " + consequent + " " + alternative + ")";
    }
}
