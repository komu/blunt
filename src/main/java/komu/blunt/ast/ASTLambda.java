package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLambda extends ASTExpression {
    public final List<Symbol> arguments;
    public final ASTExpression body;

    public ASTLambda(List<Symbol> arguments, ASTExpression body) {
        this.arguments = checkNotNull(arguments);
        this.body = checkNotNull(body);
    }

    @Override
    public String toString() {
        return "(lambda " + arguments + " " + body + ")";
    }
}
