package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.VariableReference;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CoreVariableExpression extends CoreExpression {
    
    private final VariableReference var;

    public CoreVariableExpression(VariableReference var) {
        this.var = checkNotNull(var);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        return env.lookup(var.name).freshInstance(env);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        instructions.loadVariable(target, var);
        instructions.finishWithLinkage(linkage);
    }
}
