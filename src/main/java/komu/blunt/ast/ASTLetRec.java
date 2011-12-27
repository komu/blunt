package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static komu.blunt.objects.Symbol.symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import komu.blunt.core.CoreExpression;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.objects.Symbol;
import komu.blunt.objects.Unit;
import komu.blunt.types.Assumptions;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

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
    public CoreExpression analyze(StaticEnvironment env) {
        return rewriteToLet().analyze(env);
    }

    @Override
    public TypeCheckResult<Type> typeCheck(final TypeCheckingContext ctx) {
        TypeCheckResult<Assumptions> rs = new BindGroup(new ArrayList<ExplicitBinding>(), bindings).typeCheckBindGroup(ctx);

        return body.typeCheck(new TypeCheckingContext(ctx.ce, ctx.tc, rs.value.join(ctx.as)));
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
