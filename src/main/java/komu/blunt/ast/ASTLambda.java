package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import java.util.List;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTLambda extends ASTExpression {
    public final List<Symbol> arguments;
    public final ASTExpression body;

    public ASTLambda(List<Symbol> arguments, ASTExpression body) {
        this.arguments = requireNonNull(arguments);
        this.body = requireNonNull(body);
    }

    @Override
    public String toString() {
        return "(lambda " + arguments + " " + body + ")";
    }
}
