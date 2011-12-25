package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreTupleExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;

import java.util.ArrayList;
import java.util.List;

public final class ASTTuple extends ASTExpression {
    
    private final List<ASTExpression> exps;

    public ASTTuple(List<ASTExpression> exps) {
        if (exps.size() < 2) throw new IllegalArgumentException("invalid sub expressions for tuple: " + exps);

        this.exps = new ArrayList<ASTExpression>(exps);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreTupleExpression(analyzeAll(exps, env, rootBindings));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(tuple");
        
        for (ASTExpression exp : exps)
            sb.append(' ').append(exp);
        
        sb.append(")");
        return sb.toString();
    }
}
