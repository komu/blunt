package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLetExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;
import komu.blunt.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLet extends ASTExpression {
    public final List<ImplicitBinding> bindings;
    public final ASTExpression body;

    public ASTLet(List<ImplicitBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env) {
        if (bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");
        
        StaticEnvironment newEnv = env.extend(getVariables());
        
        ImplicitBinding binding = bindings.get(0);

        return new CoreLetExpression(binding.name, binding.expr.analyze(env), body.analyze(newEnv));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        if (bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");
        
        Symbol arg = bindings.get(0).name;
        ASTExpression exp = bindings.get(0).expr;

        TypeCheckResult<Type> expResult = exp.typeCheck(ctx);
        
        Assumptions as2 = Assumptions.singleton(arg, expResult.value.toScheme());

        TypeCheckResult<Type> result = body.typeCheck(ctx.extend(as2));

        return new TypeCheckResult<Type>(CollectionUtils.append(expResult.predicates, result.predicates), result.value);
    }

    private List<Symbol> getVariables() {
        List<Symbol> vars = new ArrayList<Symbol>(bindings.size());
        for (ImplicitBinding binding : bindings)
            vars.add(binding.name);
        return vars;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(let (");

        for (Iterator<ImplicitBinding> iterator = bindings.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(' ');
        }
        sb.append(") ").append(body).append(')');
        
        return sb.toString();
    }
}
