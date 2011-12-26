package komu.blunt.types;

import java.util.List;

import static java.util.Collections.emptyList;

public final class TypeCheckResult<T> {

    public final List<Predicate> predicates;
    public final T value;
    
    public TypeCheckResult(List<Predicate> predicates, T value) {
        this.predicates = predicates;
        this.value = value;
    }
    
    public TypeCheckResult(T value) {
        this.predicates = emptyList();
        this.value = value;
    }
}
