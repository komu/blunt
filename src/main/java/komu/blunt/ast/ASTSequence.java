package komu.blunt.ast;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSequence extends ASTExpression {
    public final List<ASTExpression> exps;

    public ASTSequence(List<ASTExpression> exps) {
        this.exps = checkNotNull(exps);
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
