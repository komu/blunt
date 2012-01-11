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

        if (!expressions.isEmpty()) {
            for (CoreExpression exp : allButLast())
                instructions.append(exp.assemble(asm, target, Linkage.NEXT));

            instructions.append(last().assemble(asm, target, linkage));
        } else {
            instructions.finishWithLinkage(linkage);
        }

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

    @Override
    public CoreExpression simplify() {
        List<CoreExpression> exps = new ArrayList<>(expressions.size());
        for (CoreExpression exp : expressions)
            if (exp != CoreEmptyExpression.INSTANCE)
                exps.add(exp.simplify());
        
        if (exps.isEmpty())
            return CoreEmptyExpression.INSTANCE;
        else if (exps.size() == 1)
            return exps.get(0);
        else
            return new CoreSequenceExpression(exps);
    }
}
