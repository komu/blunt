package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

public final class CoreTupleExpression extends CoreExpression {
    private final List<CoreExpression> exps;

    public CoreTupleExpression(List<CoreExpression> exps) {
        this.exps = new ArrayList<CoreExpression>(exps);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        return Type.tupleType(typeCheckAll(exps, env));
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        //instructions.pushRegister(Register.ARGV);
        //instructions.pushRegister(Register.VAL);

        instructions.loadNewArray(Register.ARGV, exps.size());
        for (int i = 0; i < exps.size(); i++) {
            instructions.pushRegister(Register.ARGV);
            instructions.pushRegister(Register.ENV);
            exps.get(i).assemble(instructions, Register.VAL, Linkage.NEXT);
            instructions.popRegister(Register.ENV);
            instructions.popRegister(Register.ARGV);
            instructions.arrayStore(Register.ARGV, i, Register.VAL);
        }

        //instructions.popRegister(Register.VAL);
        //instructions.popRegister(Register.ARGV);

        if (target != Register.ARGV)
            instructions.copy(target, Register.ARGV);

        instructions.finishWithLinkage(linkage);
    }
}
