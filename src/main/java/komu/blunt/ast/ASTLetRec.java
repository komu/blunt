package komu.blunt.ast;

import komu.blunt.types.DataTypeDefinitions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTLetRec extends ASTExpression {
    public final List<ImplicitBinding> bindings;
    public final ASTExpression body;

    ASTLetRec(List<ImplicitBinding> bindings, ASTExpression body) {
        this.bindings = checkNotNull(bindings);
        this.body = checkNotNull(body);
    }

    @Override
    public <R, C> R accept(ASTVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    public ASTLet rewriteToLet() {
        List<ImplicitBinding> newBindings = new ArrayList<ImplicitBinding>(bindings.size());

        ASTSequence bodyExps = AST.sequence();
        for (ImplicitBinding binding : bindings) {
            newBindings.add(new ImplicitBinding(binding.name, AST.apply(AST.variable("unsafe-null"), AST.constructor(DataTypeDefinitions.UNIT))));
            bodyExps.add(AST.set(binding.name, binding.expr));
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
