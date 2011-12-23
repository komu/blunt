package komu.blunt.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeScheme {

    private final List<Kind> kinds;
    private final Type type;
    
    public TypeScheme(List<Kind> kinds, Type type) {
        this.kinds = new ArrayList<Kind>(kinds);
        this.type = checkNotNull(type);
    }

    // TODO
    public TypeScheme(Type type) {
        this(Collections.<Kind>emptyList(), type);
    }
    
    public Type freshInstance(TypeEnvironment env) {
        List<TypeVariable> vars = new ArrayList<TypeVariable>(kinds.size());

        for (Kind kind : kinds)
            vars.add(env.newVar(kind));

        return type.instantiate(vars);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof TypeScheme) {
            TypeScheme rhs = (TypeScheme) obj;

            return kinds.equals(rhs.kinds)
                && type.equals(rhs.type);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return kinds.hashCode() * 79 + type.hashCode();
    }
}
