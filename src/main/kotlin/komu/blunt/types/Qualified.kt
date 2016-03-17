package komu.blunt.types

import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.Substitutions
import java.util.*
import java.util.Collections.emptyList
import java.util.Objects.hash

class Qualified<out T : Types<T>>(predicates: List<Predicate>, val value: T) : Types<Qualified<T>> {

    val predicates: List<Predicate> = ArrayList<Predicate>(predicates)

    companion object {
        fun <T : Types<T>> simple(value: T) = Qualified(emptyList(), value)
    }

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        for (p in predicates)
            p.addTypeVariables(result)

        value.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Qualified<T> =
        Qualified(predicates.map { it.apply(substitution) }, value.apply(substitution))

    override fun toString(): String {
        val sb = StringBuilder()

        if (predicates.any())
            predicates.joinTo(sb, ", ", "(", ") => ")

        sb.append(value)

        return sb.toString()
    }

    override fun equals(other: Any?) =
        other is Qualified<*> && value == other.value && predicates == other.predicates

    override fun hashCode() = hash(predicates, value)
}

fun Qualified<Type>.instantiate(ts: List<TypeVariable>): Qualified<Type> =
    Qualified(predicates.map { it.instantiate(ts) }, value.instantiate(ts))

fun Qualified<Type>.quantifyAll(): Scheme =
    quantify(typeVariables)

fun Qualified<Type>.quantify(vs: Collection<TypeVariable>): Scheme {
    val vars = typeVariables.filter { it in vs }
    return Scheme(vars.kinds(), apply(Substitutions.fromTypeVariables(vars)))
}
