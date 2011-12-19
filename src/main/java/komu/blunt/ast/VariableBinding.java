package komu.blunt.ast;

import komu.blunt.objects.Symbol;

import static komu.blunt.utils.Objects.requireNonNull;

public final class VariableBinding {
    
    public final Symbol name;
    public final Expression value;

    public VariableBinding(Symbol name, Expression value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }
}
