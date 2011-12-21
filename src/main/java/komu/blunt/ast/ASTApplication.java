package komu.blunt.ast;

import java.util.Arrays;
import java.util.List;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final List<ASTExpression> args;

    public ASTApplication(ASTExpression func, List<ASTExpression> args) {
        this.func = requireNonNull(func);
        this.args = requireNonNull(args);
    }

    public ASTApplication(ASTExpression func, ASTExpression... args) {
        this(func, Arrays.asList(args));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(func);

        for (ASTExpression arg : args)
            sb.append(' ').append(arg);

        sb.append(')');
        return sb.toString();
    }
}
