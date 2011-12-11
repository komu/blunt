package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.eval.types.Type;

import java.util.ArrayList;
import java.util.List;

final class StaticBindings {

    private final List<StaticBinding> bindings = new ArrayList<StaticBinding>();

    public void bind(String name, Type type, Object value) {
        bindings.add(new StaticBinding(name, type, value));
    }

    public Environments createEnvironments() {
        StaticEnvironment staticEnvironment = new StaticEnvironment();
        Environment runtimeEnvironment = new Environment(bindings.size());

        for (StaticBinding binding : bindings) {
            VariableReference ref = staticEnvironment.define(binding.name, binding.type);
            runtimeEnvironment.set(ref, binding.value);
        }

        return new Environments(staticEnvironment, runtimeEnvironment);
    }

}
