package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Label;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.eval.Binding;
import fi.evident.dojolisp.objects.Symbol;
import fi.evident.dojolisp.types.*;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class LambdaExpression extends Expression {

    private final List<Symbol> argumentNames;
    private final Expression body;

    public LambdaExpression(Binding[] bindings, Expression body) {
        argumentNames = new ArrayList<Symbol>(bindings.length);
        for (Binding binding : bindings)
            argumentNames.add(binding.name);
        this.body = requireNonNull(body);
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        TypeEnvironment bodyEnv = new TypeEnvironment(env);

        List<Type> argumentTypes = new ArrayList<Type>(argumentNames.size());
        for (Symbol symbol : argumentNames) {
            TypeVariable var = TypeVariable.newVar(Kind.STAR);
            env.bind(symbol, new TypeScheme(var));
            argumentTypes.add(var);
        }

        return Type.makeFunctionType(argumentTypes, body.typeCheck(bodyEnv), false);
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
