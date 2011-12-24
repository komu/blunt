package komu.blunt.ast;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Unit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.objects.Symbol.symbol;

public final class ASTLetRec extends ASTExpression {
    public final List<ASTBinding> bindings;
    public final ASTExpression body;

    public ASTLetRec(List<ASTBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        return rewriteToLet().analyze(env, rootBindings);
    }

    private ASTLet rewriteToLet() {
        List<ASTBinding> newBindings = new ArrayList<ASTBinding>(bindings.size());

        ASTSequence bodyExps = new ASTSequence();
        for (ASTBinding binding : bindings) {
            newBindings.add(new ASTBinding(binding.name, new ASTApplication(new ASTVariable(symbol("unsafe-null")), new ASTConstant(Unit.INSTANCE))));
            bodyExps.add(new ASTSet(binding.name, binding.expr));
        }

        bodyExps.add(body);

        return new ASTLet(newBindings, bodyExps);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(letrec (");

        for (Iterator<ASTBinding> iterator = bindings.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(' ');
        }
        sb.append(") ").append(body).append(')');

        return sb.toString();
    }
}
