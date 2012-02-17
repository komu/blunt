package komu.blunt.types.checker

import com.google.common.collect.ImmutableList
import komu.blunt.types.Predicate

import java.util.List
import java.util.ArrayList
import java.util.Collection

class TypeCheckResult<out T>(val value: T, val predicates: ImmutableList<Predicate?>) {

    class object {
        fun builder<T>() = Builder<T>()

        fun of<T>(value: T) = TypeCheckResult(value, ImmutableList.of<Predicate?>().sure())
        fun of<T>(value: T, predicates: List<Predicate?>) = TypeCheckResult(value, ImmutableList.copyOf(predicates).sure())

        class Builder<T> private () {
            private val predicates = ArrayList<Predicate?>

            fun addPredicates(ps: Collection<Predicate?>): Builder<T> {
                predicates.addAll(ps)
                return this;
            }

            fun build(t: T) = of(t, predicates)
        }
    }

    fun withAddedPredicates(predicates: List<Predicate?>): TypeCheckResult<T> {
        val builder = builder<T>();
        builder.addPredicates(predicates);
        builder.addPredicates(this.predicates);
        return builder.build(value);
    }

    fun toString() = "$predicates => $value";
}
