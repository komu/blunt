package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreLetExpression extends CoreExpression {

    private final int envSize;
    private final CoreExpression value;
    private final CoreExpression body;

    public CoreLetExpression(int envSize, CoreExpression value, CoreExpression body) {
        this.envSize = envSize;
        this.value = checkNotNull(value);
        this.body = checkNotNull(body);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: let is implemented as lambda, which is not quite optimal

        CoreExpression func = new CoreLambdaExpression(envSize, body);
        new CoreApplicationExpression(func, value).assemble(instructions, target, linkage);
    }
}
