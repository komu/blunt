package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLambda extends ASTExpression {
    public final List<Symbol> arguments;
    public final ASTExpression body;

    ASTLambda(List<Symbol> arguments, ASTExpression body) {
        if (arguments.isEmpty()) throw new IllegalArgumentException("no arguments for lambda");

        this.arguments = checkNotNull(arguments);
        this.body = checkNotNull(body);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }
    
    public ASTExpression rewrite() {
        return AST.lambda(arguments.get(0), AST.lambda(arguments.subList(1, arguments.size()), body));
    }

    @Override
    public String toString() {
        return "(lambda " + arguments + " " + body + ")";
    }
}
