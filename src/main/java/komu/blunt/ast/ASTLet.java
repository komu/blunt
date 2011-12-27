package komu.blunt.ast;

import komu.blunt.objects.Symbol;

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
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public List<Symbol> getVariables() {
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
