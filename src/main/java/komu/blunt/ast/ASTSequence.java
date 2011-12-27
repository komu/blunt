package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreSequenceExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.SyntaxException;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

public final class ASTSequence extends ASTExpression {
    public final List<ASTExpression> exps;

    public ASTSequence(ASTExpression... exps) {
        this(asList(exps));
    }
    
    public ASTSequence(List<ASTExpression> exps) {
        this.exps = new ArrayList<ASTExpression>(exps);
        for (ASTExpression exp : exps)
            checkNotNull(exp);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public void add(ASTExpression exp) {
        exps.add(checkNotNull(exp));
    }

    public ASTExpression last() {
        return exps.get(exps.size()-1);
    }

    public List<ASTExpression> allButLast() {
        return exps.subList(0, exps.size()-1);
    }
    
    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        if (exps.isEmpty()) throw new SyntaxException("empty sequence");
        
        return new CoreSequenceExpression(analyzeAll(exps, env));
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
