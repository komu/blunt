package komu.blunt.ast;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSequence extends ASTExpression {
    public final List<ASTExpression> exps;

    ASTSequence(List<ASTExpression> exps) {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("(begin");
        
        for (ASTExpression exp : exps)
            sb.append(' ').append(exp);

        sb.append(')');
        
        return sb.toString();
    }
}
