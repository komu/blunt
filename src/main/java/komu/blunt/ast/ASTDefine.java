package komu.blunt.ast;

import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.eval.RootBindings;
import komu.blunt.eval.StaticEnvironment;
import komu.blunt.eval.VariableReference;
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
    public CoreExpression analyze(StaticEnvironment env, RootBindings rootBindings) {
        VariableReference var = rootBindings.staticEnvironment.define(name);
        return new CoreDefineExpression(name, value.analyze(env, rootBindings), var, rootBindings);
    }

    @Override
    public String toString() {
        return "(define " + name + " " + value + ")";
    }
}
