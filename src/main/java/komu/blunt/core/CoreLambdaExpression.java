package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Label;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreLambdaExpression extends CoreExpression {

    private final int envSize;
    private final CoreExpression body;

    public CoreLambdaExpression(int envSize, CoreExpression body) {
        checkArgument(envSize >= 0);

        this.envSize = envSize;
        this.body = checkNotNull(body);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: place the lambda in a new code section
        Label lambda = instructions.newLabel("lambda");
        Label afterLambda = instructions.newLabel("after-lambda");

        instructions.loadLambda(target, lambda);
        if (linkage == Linkage.NEXT) {
            instructions.jump(afterLambda);
        } else {
            instructions.finishWithLinkage(linkage);
        }

        instructions.label(lambda);
        instructions.createEnvironment(envSize);
        body.assemble(instructions, Register.VAL, Linkage.RETURN);
        instructions.label(afterLambda);
    }
}
