package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreSetExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSet extends ASTExpression {
    public final Symbol var;
    public final ASTExpression exp;

    public ASTSet(Symbol var, ASTExpression exp) {
        this.var = checkNotNull(var);
        this.exp = checkNotNull(exp);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreSetExpression(env.lookup(var), exp.analyze(env));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        // TODO: assume sets is always correct since it's auto-generated
        return new TypeCheckResult<Type>(Type.UNIT);
    }

    @Override
    public String toString() {
        return "(set! " + var + " " + exp + ")";
    }
}
