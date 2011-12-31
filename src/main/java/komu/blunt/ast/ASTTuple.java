package komu.blunt.ast;

import komu.blunt.types.DataTypeDefinitions;

import java.util.ArrayList;
import java.util.List;

public final class ASTTuple extends ASTExpression {
    
    public final List<ASTExpression> exps;

    ASTTuple(List<ASTExpression> exps) {
        if (exps.size() < 2) throw new IllegalArgumentException("invalid sub expressions for tuple: " + exps);

        this.exps = new ArrayList<ASTExpression>(exps);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return rewrite().accept(visitor, ctx);
    }
    
    private ASTExpression rewrite() {
        String name = DataTypeDefinitions.tupleName(exps.size());
        ASTExpression call =  AST.constructor(name);
        
        for (ASTExpression exp : exps)
            call = new ASTApplication(call, exp);

        return call;
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
