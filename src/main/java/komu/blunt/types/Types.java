package komu.blunt.types;

import java.util.Set;

public interface Types<T extends Types<T>> {
    void addTypeVariables(Set<TypeVariable> variables);
    T apply(Substitution substitution);
}
