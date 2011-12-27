package komu.blunt.ast;

import java.util.ArrayList;
import java.util.List;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;

public abstract class ASTExpression {

    public abstract CoreExpression analyze(StaticEnvironment env);
    public abstract TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as);

    @Override
    public abstract String toString();
    
    protected static List<CoreExpression> analyzeAll(List<ASTExpression> exps, StaticEnvironment env) {
        List<CoreExpression> result = new ArrayList<CoreExpression>(exps.size());
        
        for (ASTExpression exp : exps)
            result.add(exp.analyze(env));

        return result;
    }
}
