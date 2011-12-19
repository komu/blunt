package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.VariableReference;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import static komu.blunt.utils.Objects.requireNonNull;

public final class VariableExpression extends Expression {
    
    private final VariableReference var;

    public VariableExpression(VariableReference var) {
        this.var = requireNonNull(var);
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
