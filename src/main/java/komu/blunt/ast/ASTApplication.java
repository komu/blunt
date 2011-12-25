package komu.blunt.ast;

import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Kind;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final ASTExpression arg;

    public ASTApplication(ASTExpression func, ASTExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreApplicationExpression(func.analyze(env, rootBindings), arg.analyze(env, rootBindings));
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        Type argType = arg.typeCheck(env);
        Type returnType = env.newVar(Kind.STAR);
        Type ty = Type.makeFunctionType(argType, returnType);

        env.unify(func.typeCheck(env), ty);

        return returnType;
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
