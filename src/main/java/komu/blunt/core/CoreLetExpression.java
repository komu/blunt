package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Assembler;
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
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();
        instructions.append(new CoreSetExpression(var, value).assemble(asm, target, Linkage.NEXT));
        instructions.append(body.assemble(asm, target, linkage));
        return instructions;
    }
}
