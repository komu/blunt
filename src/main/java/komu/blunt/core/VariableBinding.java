package komu.blunt.core;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class VariableBinding {
    
    public final Symbol name;
    public final CoreExpression value;

    public VariableBinding(Symbol name, CoreExpression value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }
}
