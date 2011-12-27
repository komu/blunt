package komu.blunt.ast;

import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final ASTExpression arg;

    public ASTApplication(ASTExpression func, ASTExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreApplicationExpression(func.analyze(env), arg.analyze(env));
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }
}
