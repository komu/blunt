package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLet extends ASTExpression {
    public final ImmutableList<ImplicitBinding> bindings;
    public final ASTExpression body;

    ASTLet(ImmutableList<ImplicitBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public ASTExpression simplify() {
        // TODO: inline constants and side-effect free stuff that is referenced only once
        ImmutableList.Builder<ImplicitBinding> simplifiedBindings = ImmutableList.builder();
        for (ImplicitBinding binding : bindings)
            simplifiedBindings.add(binding.simplify());
        return new ASTLet(simplifiedBindings.build(), body.simplify());
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
