package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final ASTExpression arg;

    ASTApplication(ASTExpression func, ASTExpression arg) {
        this.func = checkNotNull(func);
        this.arg = checkNotNull(arg);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
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
            
            return AST.let(false, new ImplicitBinding(lambda.argument, simplifiedArg), lambda.body).simplify();
        }
        return new ASTApplication(simplifiedFunc, simplifiedArg);
    }
}
