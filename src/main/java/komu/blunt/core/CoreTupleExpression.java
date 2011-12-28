package komu.blunt.core;

import com.google.common.base.Strings;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.reverse;

public final class CoreTupleExpression extends CoreExpression {
    private final List<CoreExpression> exps;

    public CoreTupleExpression(List<CoreExpression> exps) {
        this.exps = new ArrayList<CoreExpression>(exps);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        for (CoreExpression exp : reverse(exps)) {
            exp.assemble(instructions, target, Linkage.NEXT);
            instructions.pushRegister(target);
        }
        
        String name = "(" + Strings.repeat(",", exps.size()) + ")";
        instructions.loadConstructed(target, name, exps.size());

        instructions.finishWithLinkage(linkage);
    }
}
