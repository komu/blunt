package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;

public final class ASTDefine {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    public CoreExpression analyze(StaticEnvironment rootEnv) {
        VariableReference var = rootEnv.define(name);
        return new CoreDefineExpression(value.analyze(rootEnv), var);
    }

    public TypeCheckResult<Type> typeCheck(ClassEnv classEnv, TypeChecker typeChecker, Assumptions assumptions) {
        return new ASTLetRec(name, value, new ASTVariable(name)).typeCheck(classEnv, typeChecker, assumptions);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
