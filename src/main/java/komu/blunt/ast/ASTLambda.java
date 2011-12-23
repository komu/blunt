package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLambdaExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
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
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        StaticEnvironment newEnv = env.extend(arguments);

        return new CoreLambdaExpression(arguments, body.analyze(newEnv, rootBindings));
    }

    @Override
    public String toString() {
        return "(lambda " + arguments + " " + body + ")";
    }
}
