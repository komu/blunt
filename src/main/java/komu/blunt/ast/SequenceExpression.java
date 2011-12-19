package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;

import java.util.ArrayList;
import java.util.List;

public final class SequenceExpression extends Expression {

    private final List<Expression> expressions;
    
    public SequenceExpression(List<Expression> expressions) {
        this.expressions = new ArrayList<Expression>(expressions);
    }
    
    @Override
    public Type typeCheck(TypeEnvironment env) {
        for (Expression exp : allButLast())
            exp.typeCheck(env);

        return last().typeCheck(env);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        for (Expression exp : allButLast())
            exp.assemble(instructions, target, Linkage.NEXT);

        last().assemble(instructions, target, linkage);
    }

    private Expression last() {
        return expressions.get(expressions.size()-1);
    }

    private List<Expression> allButLast() {
        return expressions.subList(0, expressions.size()-1);
    }
}
