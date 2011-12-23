package komu.blunt.ast;

import komu.blunt.core.CoreApplicationExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTApplication extends ASTExpression {

    public final ASTExpression func;
    public final List<ASTExpression> args;

    public ASTApplication(ASTExpression func, List<ASTExpression> args) {
        this.func = checkNotNull(func);
        this.args = checkNotNull(args);
    }

    public ASTApplication(ASTExpression func, ASTExpression... args) {
        this(func, Arrays.asList(args));
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return new CoreApplicationExpression(func.analyze(env, rootBindings), analyzeAll(args, env, rootBindings));
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
