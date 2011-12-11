package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.FunctionType;
import fi.evident.dojolisp.types.Type;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class ApplicationExpression extends Expression {
    
    private final Expression func;
    private final List<Expression> args;

    public ApplicationExpression(Expression func, List<Expression> args) {
        this.func = requireNonNull(func);
        this.args = requireNonNull(args);
    }

    @Override
    public Type typeCheck() {
        FunctionType funcType = func.typeCheck().asFunctionType();
        List<Type> argTypes = typeCheckArgs();

        return funcType.typeCheckCall(argTypes);
    }
    
    private List<Type> typeCheckArgs() {
        List<Type> types = new ArrayList<Type>(args.size());
        
        for (Expression arg : args)
            types.add(arg.typeCheck());
        
        return types;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: preserve registers
        func.assemble(instructions, Register.PROCEDURE, Linkage.NEXT);

        instructions.loadNewArray(Register.ARGV, args.size());
        for (int i = 0; i < args.size(); i++) {
            args.get(i).assemble(instructions, Register.VAL, Linkage.NEXT);
            instructions.arrayStore(Register.ARGV, i, Register.VAL);
        }

        // TODO: tail calls

        instructions.apply(Register.PROCEDURE, Register.ARGV);
        if (target != Register.VAL)
            instructions.copy(target, Register.VAL);

        instructions.finishWithLinkage(linkage);
    }
}
