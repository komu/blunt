package fi.evident.dojolisp.ast;

import fi.evident.dojolisp.eval.Binding;
import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.objects.Lambda;
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
    public Object evaluate(Environment env) {
        return new Lambda(arguments.size(), body, env);
    }

    @Override
    public Type typeCheck() {
        return new FunctionType(arguments, body.typeCheck(), false);
    }
}
