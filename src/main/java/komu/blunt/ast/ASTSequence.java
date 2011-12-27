package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreSequenceExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.SyntaxException;
import komu.blunt.types.Predicate;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

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

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        
        for (ASTExpression exp : allButLast())
            predicates.addAll(exp.typeCheck(ctx).predicates);

        TypeCheckResult<Type> result = last().typeCheck(ctx);
        
        predicates.addAll(result.predicates);

        return new TypeCheckResult<Type>(predicates, result.value);
    }

    private ASTExpression last() {
        return exps.get(exps.size()-1);
    }

    private List<ASTExpression> allButLast() {
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
