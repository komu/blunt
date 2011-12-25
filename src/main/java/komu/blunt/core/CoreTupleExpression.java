package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.reverse;

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
        for (CoreExpression exp : reverse(exps)) {
            exp.assemble(instructions, target, Linkage.NEXT);
            instructions.pushRegister(target);
        }
        instructions.loadTuple(target, exps.size());

        instructions.finishWithLinkage(linkage);
    }
}
