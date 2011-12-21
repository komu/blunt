package komu.blunt.core;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

public final class CoreSequenceExpression extends CoreExpression {

    private final List<CoreExpression> expressions;
    
    public CoreSequenceExpression(List<CoreExpression> expressions) {
        this.expressions = new ArrayList<CoreExpression>(expressions);
    }
    
    @Override
    public Type typeCheck(TypeEnvironment env) {
        for (CoreExpression exp : allButLast())
            exp.typeCheck(env);

        return last().typeCheck(env);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        for (CoreExpression exp : allButLast())
            exp.assemble(instructions, target, Linkage.NEXT);

        last().assemble(instructions, target, linkage);
    }

    private CoreExpression last() {
        return expressions.get(expressions.size()-1);
    }

    private List<CoreExpression> allButLast() {
        return expressions.subList(0, expressions.size()-1);
    }
}
