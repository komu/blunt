package komu.blunt.types;

import komu.blunt.types.checker.Substitution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Scheme implements Types<Scheme> {

    public final List<Kind> kinds;
    public final Qualified<Type> type;
    
    public Scheme(List<Kind> kinds, Qualified<Type> type) {
        this.kinds = new ArrayList<>(kinds);
        this.type = checkNotNull(type);
    }

    @Override
    public void addTypeVariables(Set<TypeVariable> variables) {
        type.addTypeVariables(variables);
    }

    @Override
    public Scheme apply(Substitution substitution) {
        return new Scheme(kinds, type.apply(substitution));
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof Scheme) {
            Scheme rhs = (Scheme) obj;

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
