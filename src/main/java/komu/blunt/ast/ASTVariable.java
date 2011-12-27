package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreVariableExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = checkNotNull(var);
    }
    
    public ASTVariable(String var) {
        this.var = symbol(var);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        return new CoreVariableExpression(env.lookup(var));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        Scheme scheme = ctx.as.find(var);
        Qualified<Type> inst = ctx.tc.freshInstance(scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
