package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Label;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.eval.Binding;
import fi.evident.dojolisp.types.FunctionType;
import fi.evident.dojolisp.types.Type;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final List<Type> arguments;
    private final Expression body;

    public LambdaExpression(Binding[] bindings, Expression body) {
        arguments = new ArrayList<Type>(bindings.length);
        for (Binding binding : bindings)
            arguments.add(binding.type);
        this.body = requireNonNull(body);
    }

    @Override
    public Type typeCheck() {
        return new FunctionType(arguments, body.typeCheck(), false);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: place the lambda in a new code section
        Label lambda = instructions.newLabel("lambda");
        Label afterLambda = instructions.newLabel("after-lambda");

        instructions.loadLambda(target, lambda);
        if (linkage == Linkage.NEXT) {
            instructions.jump(afterLambda);
        } else {
            instructions.finishWithLinkage(linkage);
        }

        instructions.label(lambda);
        body.assemble(instructions, Register.VAL, Linkage.RETURN);
        instructions.label(afterLambda);
    }
}
