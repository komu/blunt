package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLetExpression;
import komu.blunt.core.VariableBinding;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLet extends ASTExpression {
    public final List<ASTBinding> bindings;
    public final ASTExpression body;

    public ASTLet(List<ASTBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        StaticEnvironment newEnv = env.extend(getVariables());

        return new CoreLetExpression(analyzeBindings(bindings, env, rootBindings), body.analyze(newEnv, rootBindings));
    }

    private static List<VariableBinding> analyzeBindings(List<ASTBinding> bindings, StaticEnvironment env, RootBindings rootBindings) {
        List<VariableBinding> result = new ArrayList<VariableBinding>(bindings.size());

        for (ASTBinding binding : bindings)
            result.add(new VariableBinding(binding.name, binding.expr.analyze(env, rootBindings)));

        return result;
    }

    private List<Symbol> getVariables() {
        List<Symbol> vars = new ArrayList<Symbol>(bindings.size());
        for (ASTBinding binding : bindings)
            vars.add(binding.name);
        return vars;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(let (");

        for (Iterator<ASTBinding> iterator = bindings.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(' ');
        }
        sb.append(")").append(body).append(')');
        
        return sb.toString();
    }
}
