package komu.blunt.types

import java.util.ArrayList
import java.util.Collections.emptyList
import java.util.Objects.hash
import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.Substitutions

class Qualified<out T : Types<T>>(predicates: List<Predicate>, val value: T) : Types<Qualified<T>> {

    public val predicates: List<Predicate> = ArrayList<Predicate>(predicates)

    class object {
        fun simple<T : Types<T>>(value: T) = Qualified<T>(emptyList(), value)
    }

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        for (p in predicates)
            p.addTypeVariables(result)

        value.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Qualified<T> =
        Qualified(predicates.map { it.apply(substitution) }, value.apply(substitution))

    fun toString(): String {
        val sb = StringBuilder()

        if (!predicates.empty)
            predicates.appendString(sb, ", ", "(", ") => ")

        sb.append(value)

        return sb.toString()
    }

    fun equals(rhs: Any?) =
        rhs is Qualified<*> && value == rhs.value && predicates == rhs.predicates

    fun hashCode() = hash(predicates, value)
}

fun Qualified<Type>.instantiate(ts: List<TypeVariable>): Qualified<Type> =
    Qualified(predicates.map { it.instantiate(ts) }, value.instantiate(ts))

fun Qualified<Type>.quantifyAll(): Scheme =
    quantify(typeVariables)

fun Qualified<Type>.quantify(vs: Collection<TypeVariable>): Scheme {
    val vars = typeVariables.filter { it in vs }
    return Scheme(vars.kinds(), apply(Substitutions.fromTypeVariables(vars)))
}
