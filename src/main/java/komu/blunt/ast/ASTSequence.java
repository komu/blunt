package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreSequenceExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.SyntaxException;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSequence extends ASTExpression {
    public final List<ASTExpression> exps;

    public ASTSequence(List<ASTExpression> exps) {
        this.exps = checkNotNull(exps);

        if (exps.isEmpty()) throw new SyntaxException("empty sequence");
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreSequenceExpression(analyzeAll(exps, env, rootBindings));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("(begin");
        
        for (ASTExpression exp : exps)
            sb.append(' ').append(exp);

        sb.append(')');
        
        return sb.toString();
    }
}
