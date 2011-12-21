package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class ASTDefine extends ASTExpression {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
