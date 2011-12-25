package komu.blunt.ast;

import com.google.common.base.Preconditions;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Unit;

import java.util.ArrayList;
import java.util.List;

public final class ASTList extends ASTExpression {

    private final List<ASTExpression> exps = new ArrayList<ASTExpression>();

    public void add(ASTExpression exp) {
        exps.add(Preconditions.checkNotNull(exp));
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return rewrite().analyze(env, rootBindings);
    }
    
    private ASTExpression rewrite() {
        ASTExpression exp = nil();
        
        for (int i = exps.size()-1; i >= 0; i--)
            exp = cons(exps.get(i), exp);
        
        return exp;
    }

    private ASTApplication nil() {
        return new ASTApplication(new ASTVariable("primitiveNil"), new ASTConstant(Unit.INSTANCE));
    }

    private ASTExpression cons(ASTExpression head, ASTExpression tail) {
        return new ASTApplication(new ASTApplication(new ASTVariable(":"), head), tail);
    }

    @Override
    public String toString() {
        return exps.toString();
    }
}
