package komu.blunt.ast;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.objects.Symbol;
import komu.blunt.types.Type;
import komu.blunt.types.TypeEnvironment;
import komu.blunt.types.TypeScheme;

import java.util.ArrayList;
import java.util.List;

public final class LetExpression extends Expression {

    private final List<VariableBinding> bindings;
    private final Expression body;

    public LetExpression(List<VariableBinding> bindings, Expression body) {
        this.bindings = new ArrayList<VariableBinding>(bindings);
        this.body = body;
    }

    @Override
    public Type typeCheck(TypeEnvironment env) {
        TypeEnvironment bodyEnv = new TypeEnvironment(env);

        for (VariableBinding binding : bindings) {
            Type type = binding.value.typeCheck(env);
            env.bind(binding.name, new TypeScheme(type));
        }

        return body.typeCheck(bodyEnv);
    }

    @Override
    public void assemble(Instructions instructions, Register target, Linkage linkage) {
        // TODO: let is implemented as lambda, which is not quite optimal

        Expression func = new LambdaExpression(variables(), body);
        new ApplicationExpression(func, values()).assemble(instructions, target, linkage);
    }
    
    private List<Symbol> variables() {
        List<Symbol> variables = new ArrayList<Symbol>(bindings.size());
        
        for (VariableBinding binding : bindings)
            variables.add(binding.name);

        return variables;
    }
    
    private List<Expression> values() {
        List<Expression> values = new ArrayList<Expression>(bindings.size());
        
        for (VariableBinding binding : bindings)
            values.add(binding.value);

        return values;
    }
}
