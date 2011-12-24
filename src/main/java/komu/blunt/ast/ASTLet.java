package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLetExpression;
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
        if (bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");
        
        StaticEnvironment newEnv = env.extend(getVariables());
        
        ASTBinding binding = bindings.get(0);

        return new CoreLetExpression(binding.name, binding.expr.analyze(env, rootBindings), body.analyze(newEnv, rootBindings));
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
        sb.append(") ").append(body).append(')');
        
        return sb.toString();
    }
}
