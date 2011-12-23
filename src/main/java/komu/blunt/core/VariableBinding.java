package komu.blunt.core;

import komu.blunt.objects.Symbol;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VariableBinding {
    
    public final Symbol name;
    public final CoreExpression value;

    public VariableBinding(Symbol name, CoreExpression value) {
        this.name = checkNotNull(name);
        this.value = checkNotNull(value);
    }
}
