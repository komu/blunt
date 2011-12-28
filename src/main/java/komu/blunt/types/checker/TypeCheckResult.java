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

    public static <T> TypeCheckResult<T> of(T value) {
        return new TypeCheckResult<T>(value, ImmutableList.<Predicate>of());
    }

    public static <T> TypeCheckResult<T> of(T value, List<Predicate> p1) {
        return new TypeCheckResult<T>(value, ImmutableList.copyOf(p1));
    }

    public static <T> TypeCheckResult<T> of(T value, List<Predicate> p1, List<Predicate> p2) {
        ImmutableList.Builder<Predicate> builder = new ImmutableList.Builder<Predicate>();
        builder.addAll(p1).addAll(p2);
        return new TypeCheckResult<T>(value, builder.build());
    }
    
    public static <T> TypeCheckResult<T> of(T value, List<Predicate> p1, List<Predicate> p2, List<Predicate> p3) {
        ImmutableList.Builder<Predicate> builder = new ImmutableList.Builder<Predicate>();
        builder.addAll(p1).addAll(p2).addAll(p3);
        return new TypeCheckResult<T>(value, builder.build());
    }

    public TypeCheckResult<T> withAddedPredicates(List<Predicate> predicates) {
        return TypeCheckResult.of(value, predicates, this.predicates);
    }

    @Override
    public String toString() {
        return predicates + " => " + value;
    }
}
