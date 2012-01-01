package komu.blunt.ast;

import com.google.common.collect.ImmutableList;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.DataTypeDefinitions.UNIT;

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

    public ASTLet rewriteToLet() {
        ImmutableList.Builder<ImplicitBinding> newBindings = ImmutableList.builder();
        AST.SequenceBuilder bodyExps = AST.sequenceBuilder();

        for (ImplicitBinding binding : bindings) {
            newBindings.add(new ImplicitBinding(binding.name, AST.apply(AST.variable("unsafe-null"), AST.constructor(UNIT))));
            bodyExps.add(AST.set(binding.name, binding.expr));
        }

        bodyExps.add(body);

        return new ASTLet(newBindings.build(), bodyExps.build());
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
