package fi.evident.dojolisp.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class TypeScheme {

    private final List<Kind> kinds;
    private final Type type;
    
    public TypeScheme(List<Kind> kinds, Type type) {
        this.kinds = new ArrayList<Kind>(kinds);
        this.type = requireNonNull(type);
    }

    // TODO
    public TypeScheme(Type type) {
        this(Collections.<Kind>emptyList(), type);
    }
    
    public Type freshInstance() {
        List<TypeVariable> vars = new ArrayList<TypeVariable>(kinds.size());
        for (Kind kind : kinds)
            vars.add(TypeVariable.newVar(kind));

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
