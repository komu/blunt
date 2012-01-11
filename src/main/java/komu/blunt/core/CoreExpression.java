package komu.blunt.core;

import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import java.util.List;

public abstract class CoreExpression {
    public abstract void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage);

    public static CoreExpression and(List<CoreExpression> exps) {
        if (exps.isEmpty())
            return new CoreConstantExpression(true);
        else if (exps.size() == 1)
            return exps.get(0);
        else
            return new CoreIfExpression(exps.get(0), and(exps.subList(1, exps.size())), new CoreConstantExpression(false));
    }
}
