package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.types.Kind;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;
import fi.evident.dojolisp.types.TypeVariable;

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
    public Type typeCheck(TypeEnvironment env) {
        List<Type> argTypes = typeCheckArgs(env);

        Type returnType = TypeVariable.newVar(Kind.STAR);

        Type ty = Type.makeFunctionType(argTypes, returnType);
        
        env.unify(func.typeCheck(env), ty);

        return returnType;
    }
    
    private List<Type> typeCheckArgs(TypeEnvironment env) {
        List<Type> types = new ArrayList<Type>(args.size());
        
        for (Expression arg : args)
            types.add(arg.typeCheck(env));
        
        return types;
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: preserve registers only if needed

        func.assemble(instructions, Register.PROCEDURE, Linkage.NEXT);
        instructions.pushRegister(Register.PROCEDURE);

        instructions.loadNewArray(Register.ARGV, args.size());
        for (int i = 0; i < args.size(); i++) {
            instructions.pushRegister(Register.ARGV);
            args.get(i).assemble(instructions, Register.VAL, Linkage.NEXT);
            instructions.popRegister(Register.ARGV);
            instructions.arrayStore(Register.ARGV, i, Register.VAL);
        }

        instructions.popRegister(Register.PROCEDURE);

        // TODO: tail calls

        instructions.apply(Register.PROCEDURE, Register.ARGV);
        if (target != Register.VAL)
            instructions.copy(target, Register.VAL);

        instructions.finishWithLinkage(linkage);
    }
}
