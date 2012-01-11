package komu.blunt.core;

import komu.blunt.asm.Assembler;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public final class CoreSequenceExpression extends CoreExpression {

    private final List<CoreExpression> expressions;
    
    public CoreSequenceExpression(List<CoreExpression> expressions) {
        this.expressions = new ArrayList<>(expressions);
    }

    public CoreSequenceExpression(CoreExpression... expressions) {
        this.expressions = asList(expressions);
    }

    @Override
    public void assemble(Assembler asm, Instructions instructions, Register target, Linkage linkage) {
        for (CoreExpression exp : allButLast())
            exp.assemble(asm, instructions, target, Linkage.NEXT);

        last().assemble(asm, instructions, target, linkage);
    }

    private CoreExpression last() {
        return expressions.get(expressions.size()-1);
    }

    private List<CoreExpression> allButLast() {
        return expressions.subList(0, expressions.size()-1);
    }

    @Override
    public String toString() {
        return expressions.toString();
    }
}
