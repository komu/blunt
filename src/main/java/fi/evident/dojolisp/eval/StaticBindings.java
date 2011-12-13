package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;
import fi.evident.dojolisp.types.TypeScheme;

import java.util.ArrayList;
import java.util.List;

final class StaticBindings {

    private final List<StaticBinding> bindings = new ArrayList<StaticBinding>();

    public void bind(String name, TypeScheme type, Object value) {
        bindings.add(new StaticBinding(name, type, value));
    }
    
    public void bind(String name, Type type, Object value) {
        bind(name, new TypeScheme(type), value);
    }

    public Environments createEnvironments() {
        StaticEnvironment staticEnvironment = new StaticEnvironment();
        TypeEnvironment typeEnvironment = new TypeEnvironment();
        Environment runtimeEnvironment = new Environment(bindings.size());

        for (StaticBinding binding : bindings) {
            VariableReference ref = staticEnvironment.define(binding.name, binding.type);
            typeEnvironment.bind(binding.name, binding.type);
            runtimeEnvironment.set(ref, binding.value);
        }

        return new Environments(staticEnvironment, typeEnvironment, runtimeEnvironment);
    }
}
