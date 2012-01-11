package komu.blunt.types.checker;

import com.google.common.collect.ImmutableList;
import komu.blunt.types.Predicate;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeCheckResult<T> {

    public final T value;
    public final ImmutableList<Predicate> predicates;

    private TypeCheckResult(T value, ImmutableList<Predicate> predicates) {
        this.value = checkNotNull(value);
        this.predicates = checkNotNull(predicates);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
    
    public static <T> TypeCheckResult<T> of(T value) {
        return new TypeCheckResult<>(value, ImmutableList.<Predicate>of());
    }

    public static <T> TypeCheckResult<T> of(T value, List<Predicate> p1) {
        Builder<T> builder = builder();
        return builder.addPredicates(p1).build(value);
    }

    public TypeCheckResult<T> withAddedPredicates(List<Predicate> predicates) {
        Builder<T> builder = builder();
        builder.addPredicates(predicates);
        builder.addPredicates(this.predicates);
        return builder.build(value);
    }

    @Override
    public String toString() {
        return predicates + " => " + value;
    }
    
    public static final class Builder<T> {
        private final ImmutableList.Builder<Predicate> predicates = ImmutableList.builder();
        
        private Builder() {
        }

        public Builder<T> addPredicates(Iterable<Predicate> ps) {
            this.predicates.addAll(ps);
            return this;
        }
        
        public TypeCheckResult<T> build(T type) {
            return new TypeCheckResult<>(type, predicates.build());
        }
    }
}
