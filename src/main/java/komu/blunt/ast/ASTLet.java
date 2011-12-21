package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTLet extends ASTExpression {
    public final List<ASTBinding> bindings;
    public final ASTExpression body;

    public ASTLet(List<ASTBinding> bindings, ASTExpression body) {
        this.bindings = requireNonNull(bindings);
        this.body = requireNonNull(body);
    }

    public List<Symbol> getVariables() {
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
