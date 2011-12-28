package komu.blunt.types;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorDefinition {
    private final String name;
    public final Scheme scheme;
    public final String primitive;

    public ConstructorDefinition(String name, Scheme scheme, String primitive) {
        this.name = checkNotNull(name);
        this.scheme = checkNotNull(scheme);
        this.primitive = checkNotNull(primitive);
    }

    @Override
    public String toString() {
        return name + " :: " + scheme;
    }
}
