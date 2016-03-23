package komu.blunt.types

import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.Substitutions
import java.util.Collections.emptyList
import java.util.Objects.hash

class Qualified<out T : Types<T>>(val predicates: List<Predicate>, val value: T) : Types<Qualified<T>> {

    companion object {
        fun <T : Types<T>> simple(value: T) = Qualified(emptyList(), value)
    }

    override fun typeVars(): Sequence<Type.Var> =
        predicates.asSequence().flatMap { it.typeVars() } + value.typeVars()

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

fun Qualified<Type>.instantiate(ts: List<Type.Var>): Qualified<Type> =
    Qualified(predicates.map { it.instantiate(ts) }, value.instantiate(ts))

fun Qualified<Type>.quantifyAll(): Scheme =
    quantify(typeVarsSet())

fun Qualified<Type>.quantify(vs: Collection<Type.Var>): Scheme {
    val vars = typeVars().filter { it in vs }.toSet()
    val kinds = vars.map { it.kind }
    return Scheme(kinds, apply(Substitutions.fromTypeVariables(vars)))
}
