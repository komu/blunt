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
    public void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage) {
        new CoreSetExpression(var, value).assemble(asm, instructions, target, Linkage.NEXT);
        body.assemble(asm, instructions, target, linkage);
    }
}
