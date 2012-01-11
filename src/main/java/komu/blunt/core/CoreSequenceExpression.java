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
    public Instructions assemble(Assembler asm, Register target, Linkage linkage) {
        Instructions instructions = new Instructions();

        for (CoreExpression exp : allButLast())
            instructions.append(exp.assemble(asm, target, Linkage.NEXT));

        instructions.append(last().assemble(asm, target, linkage));

        return instructions;
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
