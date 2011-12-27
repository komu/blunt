package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.objects.Unit;

public final class CoreSetExpression extends CoreExpression {

    private final VariableReference var;
    private final CoreExpression exp;

    public CoreSetExpression(VariableReference var, CoreExpression exp) {
        this.var = var;
        this.exp = exp;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        instructions.pushRegister(Register.VAL); // TODO: only save var if needed

        exp.assemble(instructions, Register.VAL, Linkage.NEXT);
        instructions.storeVariable(var, Register.VAL);

        instructions.popRegister(Register.VAL);

        instructions.loadConstant(target, Unit.INSTANCE);
        instructions.finishWithLinkage(linkage);
    }
}
