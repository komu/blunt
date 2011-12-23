package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ASTDefine extends ASTExpression {

    public final Symbol name;
    public final ASTExpression value;

    public ASTDefine(Symbol name, ASTExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
