package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTSequence extends ASTExpression {
    public final ImmutableList<ASTExpression> exps;

    ASTSequence(ImmutableList<ASTExpression> exps) {
        this.exps = checkNotNull(exps);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public ASTExpression last() {
        return exps.get(exps.size()-1);
    }

    public ImmutableList<ASTExpression> allButLast() {
        return exps.subList(0, exps.size()-1);
    }

    @Override
    public ASTExpression simplify() {
        ImmutableList.Builder<ASTExpression> builder = ImmutableList.builder();

        for (ASTExpression exp : exps)
            builder.add(exp.simplify());
        
        ImmutableList<ASTExpression> result = builder.build();
        if (result.size() == 1)
            return result.get(0);
        else
            return new ASTSequence(result);
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
