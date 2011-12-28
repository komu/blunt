package komu.blunt.types;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorDefinition {
    public final String name;
    public final Scheme scheme;
    public final int arity;

    public ConstructorDefinition(String name, Scheme scheme, int arity) {
        checkArgument(arity >= 0);
        this.name = checkNotNull(name);
        this.scheme = checkNotNull(scheme);
        this.arity = arity;
    }

    @Override
    public String toString() {
        return name + " :: " + scheme;
    }
}
