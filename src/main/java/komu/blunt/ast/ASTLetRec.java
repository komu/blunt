package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLetRec extends ASTExpression {
    public final ImmutableList<ImplicitBinding> bindings;
    public final ASTExpression body;

    ASTLetRec(ImmutableList<ImplicitBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public ASTExpression simplify() {
        ImmutableList.Builder<ImplicitBinding> simplifiedBindings = ImmutableList.builder();
        for (ImplicitBinding binding : bindings)
            simplifiedBindings.add(binding.simplify());
        return new ASTLetRec(simplifiedBindings.build(), body.simplify());
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
