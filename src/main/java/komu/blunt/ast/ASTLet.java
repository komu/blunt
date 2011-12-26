package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.core.CoreLetExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;
import komu.blunt.utils.ListUtils;

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
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        if (bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");
        
        StaticEnvironment newEnv = env.extend(getVariables());
        
        ImplicitBinding binding = bindings.get(0);

        return new CoreLetExpression(binding.name, binding.expr.analyze(env, rootBindings), body.analyze(newEnv, rootBindings));
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        if (bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");
        
        Symbol arg = bindings.get(0).name;
        ASTExpression exp = bindings.get(0).expr;

        TypeCheckResult<Type> expResult = exp.typeCheck(ce, tc, as);
        
        // TODO: is this toScheme correct?
        Assumptions as2 = as.extend(arg, expResult.value.toScheme());

        TypeCheckResult<Type> result = body.typeCheck(ce, tc, as2);

        return new TypeCheckResult<Type>(ListUtils.append(expResult.predicates, result.predicates), result.value);
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
