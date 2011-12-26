package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.List;

public abstract class ASTExpression {

    public abstract CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings);
    public abstract TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as);

    @Override
    public abstract String toString();
    
    protected static List<CoreExpression> analyzeAll(List<ASTExpression> exps, StaticEnvironment env, RootBindings rootBindings) {
        List<CoreExpression> result = new ArrayList<CoreExpression>(exps.size());
        
        for (ASTExpression exp : exps)
            result.add(exp.analyze(env, rootBindings));

        return result;
    }
}
