package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreVariableExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = checkNotNull(var);
    }
    
    public ASTVariable(String var) {
        this.var = symbol(var);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreVariableExpression(env.lookup(var));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        Scheme scheme = ctx.find(var);
        Qualified<Type> inst = ctx.freshInstance(scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
