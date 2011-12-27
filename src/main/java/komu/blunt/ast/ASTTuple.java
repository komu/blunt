package komu.blunt.ast;

import java.util.ArrayList;
import java.util.List;

public final class ASTTuple extends ASTExpression {
    
    public final List<ASTExpression> exps;

    public ASTTuple(List<ASTExpression> exps) {
        if (exps.size() < 2) throw new IllegalArgumentException("invalid sub expressions for tuple: " + exps);

        this.exps = new ArrayList<ASTExpression>(exps);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
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
