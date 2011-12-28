package komu.blunt.core;

import static com.google.common.collect.Lists.reverse;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

public final class CoreConstructorExpression extends CoreExpression {

    private final String name;
    private final List<CoreExpression> exps;

    public CoreConstructorExpression(String name, List<CoreExpression> exps) {
        this.name = Preconditions.checkNotNull(name);
        this.exps = new ArrayList<CoreExpression>(exps);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        for (CoreExpression exp : reverse(exps)) {
            exp.assemble(instructions, target, Linkage.NEXT);
            instructions.pushRegister(target);
        }
        
        instructions.loadConstructed(target, name, exps.size());

        instructions.finishWithLinkage(linkage);
    }
}
