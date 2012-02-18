package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTValueDefinition extends ASTDefinition {

    public final Symbol name;
    public final ASTExpression value;

    ASTValueDefinition(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
