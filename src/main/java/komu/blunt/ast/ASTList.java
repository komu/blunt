package komu.blunt.ast;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import komu.blunt.types.DataTypeDefinitions;

public final class ASTList extends ASTExpression {

    private final List<ASTExpression> exps = new ArrayList<ASTExpression>();

    public void add(ASTExpression exp) {
        exps.add(Preconditions.checkNotNull(exp));
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public ASTExpression rewrite() {
        ASTExpression exp = new ASTConstructor(DataTypeDefinitions.NIL);
        
        for (int i = exps.size()-1; i >= 0; i--)
            exp = cons(exps.get(i), exp);
        
        return exp;
    }

    private ASTExpression cons(ASTExpression head, ASTExpression tail) {
        return new ASTApplication(new ASTApplication(new ASTConstructor(DataTypeDefinitions.CONS), head), tail);
    }

    @Override
    public String toString() {
        return exps.toString();
    }
}
