package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final ASTExpression arg;

    ASTApplication(ASTExpression func, ASTExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public String toString() {
        return "(" + func + " " + arg + ")";
    }

    @Override
    public ASTExpression simplify() {
        ASTExpression simplifiedFunc = func.simplify();
        ASTExpression simplifiedArg = arg.simplify();
     
        if (simplifiedFunc instanceof ASTLambda) {
            ASTLambda lambda = (ASTLambda) simplifiedFunc;

            return new ASTLet(ImmutableList.of(new ImplicitBinding(lambda.argument, simplifiedArg)), lambda.body).simplify();
        }
        return new ASTApplication(simplifiedFunc, simplifiedArg);
    }
}
