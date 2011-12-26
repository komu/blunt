package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreLetExpression extends CoreExpression {

    private final Symbol name;
    private final CoreExpression value;
    private final CoreExpression body;

    public CoreLetExpression(Symbol name, CoreExpression value, CoreExpression body) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
        this.body = checkNotNull(body);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: let is implemented as lambda, which is not quite optimal

        CoreExpression func = new CoreLambdaExpression(name, body);
        new CoreApplicationExpression(func, value).assemble(instructions, target, linkage);
    }
}
