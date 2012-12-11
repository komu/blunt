package komu.blunt.types.checker

import java.util.ArrayList
import java.util.Collections.emptyList
import komu.blunt.types.Predicate

class TypeCheckResult<out T>(val value: T, val predicates: List<Predicate>) {

    class object {
        fun builder<T>() = Builder<T>()

        fun of<T>(value: T) = TypeCheckResult(value, emptyList())
        fun of<T>(value: T, predicates: List<Predicate>) = TypeCheckResult(value, predicates)
        fun of<T>(value: T, vararg predicateCollections: Collection<Predicate>): TypeCheckResult<T> {
            val list = ArrayList<Predicate>()
            for (ps in predicateCollections)
                list.addAll(ps)
            return TypeCheckResult(value, list)
        }

        class Builder<T> () {
            private val predicates = ArrayList<Predicate>()

            fun addPredicates(ps: Collection<Predicate>): Builder<T> {
                predicates.addAll(ps)
                return this
            }

            fun build(t: T) = of(t, predicates)
        }
    }

    fun component1() = value
    fun component2() = predicates

    fun withAddedPredicates(predicates: List<Predicate>): TypeCheckResult<T> {
        val builder = builder<T>()
        builder.addPredicates(predicates)
        builder.addPredicates(this.predicates)
        return builder.build(value)
    }

    fun toString() = "$predicates => $value"
}
