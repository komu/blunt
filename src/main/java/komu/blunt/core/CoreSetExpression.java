package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.eval.VariableReference;
import komu.blunt.objects.Unit;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

public final class CoreSetExpression extends CoreExpression {

    private final VariableReference var;
    private final CoreExpression exp;

    public CoreSetExpression(VariableReference var, CoreExpression exp) {
        this.var = var;
        this.exp = exp;
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        // TODO: is it correct to create fresh instantiation of the variable?
        Type varType = env.lookup(var.name).freshInstance(env);

        env.unify(varType, exp.typeCheck(env));
        return Type.UNIT;
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
