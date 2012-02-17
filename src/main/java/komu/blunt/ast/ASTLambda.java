package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLambda extends ASTExpression {
    public final Symbol argument;
    public final ASTExpression body;

    ASTLambda(Symbol argument, ASTExpression body) {
        this.argument = checkNotNull(argument);
        this.body = checkNotNull(body);
    }

    @Override
    public String toString() {
        return "(lambda " + argument + " " + body + ")";
    }

    @Override
    public ASTExpression simplify() {
        return new ASTLambda(argument, body.simplify());
    }
}
