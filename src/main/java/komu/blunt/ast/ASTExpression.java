package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

public abstract class ASTExpression {

    public abstract CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings);
    public abstract Type typeCheck(TypeEnvironment env);

    @Override
    public abstract String toString();
    
    protected static List<CoreExpression> analyzeAll(List<ASTExpression> exps, StaticEnvironment env, RootBindings rootBindings) {
        List<CoreExpression> result = new ArrayList<CoreExpression>(exps.size());
        
        for (ASTExpression exp : exps)
            result.add(exp.analyze(env, rootBindings));

        return result;
    }

    protected static List<Type> typeCheckAll(List<ASTExpression> exps, TypeEnvironment env) {
        List<Type> types = new ArrayList<Type>(exps.size());

        for (ASTExpression exp : exps)
            types.add(exp.typeCheck(env));

        return types;
    }
}
