package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTCase extends ASTExpression {

    public final ASTExpression exp;
    public final ImmutableList<ASTAlternative> alternatives;

    ASTCase(ASTExpression exp, ImmutableList<ASTAlternative> alternatives) {
        this.exp = checkNotNull(exp);
        this.alternatives = checkNotNull(alternatives);
    }

    @Override
    public ASTExpression simplify() {
        ImmutableList.Builder<ASTAlternative> alts = ImmutableList.builder();
        
        for (ASTAlternative alt : alternatives)
            alts.add(alt.simplify());

        return new ASTCase(exp.simplify(), alts.build());
    }

    @Override
    public String toString() {
        return "case " + exp + " of " + alternatives;
    }
}
