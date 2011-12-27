package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreVariableExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;

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
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        Scheme scheme = as.find(var);
        Qualified<Type> inst = tc.freshInstance(scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    @Override
    public String toString() {
        return var.toString();
    }
}
