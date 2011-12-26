package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;

public final class ASTLetRec extends ASTExpression {
    public final List<ImplicitBinding> bindings;
    public final ASTExpression body;

    public ASTLetRec(List<ImplicitBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    public ASTLetRec(Symbol name, ASTExpression value, ASTExpression body) {
        this(asList(new ImplicitBinding(name, value)), body);
    }
    
    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return rewriteToLet().analyze(env, rootBindings);
    }

    @Override
    public TypeCheckResult<Type> typeCheck(ClassEnv ce, TypeChecker tc, Assumptions as) {
        TypeCheckResult<Assumptions> rs = new BindGroup(new ArrayList<ExplicitBinding>(), bindings).typeCheckBindGroup(ce, tc, as);

        return body.typeCheck(ce, tc, rs.value.join(as));
    }

    private ASTLet rewriteToLet() {
        List<ImplicitBinding> newBindings = new ArrayList<ImplicitBinding>(bindings.size());

        ASTSequence bodyExps = new ASTSequence();
        for (ImplicitBinding binding : bindings) {
            newBindings.add(new ImplicitBinding(binding.name, new ASTApplication(new ASTVariable(symbol("unsafe-null")), new ASTConstant(Unit.INSTANCE))));
            bodyExps.add(new ASTSet(binding.name, binding.expr));
        }

        bodyExps.add(body);

        return new ASTLet(newBindings, bodyExps);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(letrec (");

        for (Iterator<ImplicitBinding> iterator = bindings.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(' ');
        }
        sb.append(") ").append(body).append(')');

        return sb.toString();
    }
}
