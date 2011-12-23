package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreVariableExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTVariable extends ASTExpression {
    public final Symbol var;

    public ASTVariable(Symbol var) {
        this.var = checkNotNull(var);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreVariableExpression(env.lookup(var));
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
