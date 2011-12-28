package komu.blunt.types;

import com.google.common.base.Preconditions;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorDefinition {
    public final String name;
    public final Scheme scheme;
    public final String primitive;
    public final int arity;

    public ConstructorDefinition(String name, Scheme scheme, String primitive, int arity) {
        Preconditions.checkArgument(arity >= 0);
        this.name = checkNotNull(name);
        this.scheme = checkNotNull(scheme);
        this.primitive = primitive;
        this.arity = arity;
    }

    @Override
    public String toString() {
        return name + " :: " + scheme;
    }
}
