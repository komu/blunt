package komu.blunt.core;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.objects.TypeConstructorValue;
import komu.blunt.types.DataTypeDefinitions;

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

        instructions.loadConstant(target, new TypeConstructorValue(DataTypeDefinitions.UNIT));
        instructions.finishWithLinkage(linkage);
    }

    @Override
    public String toString() {
        return "(set! " + var.name + " " + exp + ")";
    }
}
