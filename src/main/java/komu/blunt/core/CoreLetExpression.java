package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreLetExpression extends CoreExpression {

    private final VariableReference var;
    private final CoreExpression value;
    private final CoreExpression body;

    public CoreLetExpression(VariableReference var, CoreExpression value, CoreExpression body) {
        this.var = checkNotNull(var);
        this.value = checkNotNull(value);
        this.body = checkNotNull(body);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: let is implemented as lambda, which is not quite optimal

        new CoreSetExpression(var, value).assemble(instructions, target, Linkage.NEXT);
        body.assemble(instructions, target, linkage);
        //CoreExpression func = new CoreLambdaExpression(20, body);
        //new CoreApplicationExpression(func, value).assemble(instructions, target, linkage);
    }
}
