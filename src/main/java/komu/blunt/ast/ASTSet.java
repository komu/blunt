package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreSetExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSet extends ASTExpression {
    public final Symbol var;
    public final ASTExpression exp;

    public ASTSet(Symbol var, ASTExpression exp) {
        this.var = checkNotNull(var);
        this.exp = checkNotNull(exp);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreSetExpression(env.lookup(var), exp.analyze(env, rootBindings));
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        // TODO: is it correct to create fresh instantiation of the variable?
        Type varType = env.lookup(var).freshInstance(env);

        env.unify(varType, exp.typeCheck(env));
        return Type.UNIT;
    }

    @Override
    public String toString() {
        return "(set! " + var + " " + exp + ")";
    }
}
