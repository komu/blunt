package komu.blunt.types

import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.Objects.hash
import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.Substitutions
import komu.blunt.utils.appendWithSeparator

fun quantifyAll(qt: Qualified<Type>): Scheme {
    val types = LinkedHashSet<TypeVariable>()
    qt.addTypeVariables(types)
    return quantify(types, qt)
}

fun quantify(vs: Collection<TypeVariable>, qt: Qualified<Type>): Scheme {
    val kinds = listBuilder<Kind>()
    val vars = listBuilder<TypeVariable>()

    for (v in qt.getTypeVariables())
        if (v in vs) {
            vars.add(v)
            kinds.add(v.kind)
        }

    return Scheme(kinds.build(), qt.apply(Substitutions.fromTypeVariables(vars.build())))
}

fun instantiate(ts: List<TypeVariable>, t: Qualified<Type>): Qualified<Type> =
    Qualified(t.predicates.map { it.instantiate(ts) }, t.value.instantiate(ts))

class Qualified<out T : Types<T>>(predicates: List<Predicate>, val value: T) : Types<Qualified<T>> {

    public val predicates: List<Predicate> = ArrayList<Predicate>(predicates)

    class object {
        fun simple<T : Types<T>>(value: T) = Qualified<T>(arrayList(), value)
    }

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        for (val p in predicates)
            p.addTypeVariables(result)

        value.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Qualified<T> =
        Qualified(TypeUtils.applySubstitution<Predicate>(substitution, predicates), value.apply(substitution))

    fun toString(): String {
        val sb = StringBuilder()

        if (!predicates.empty)
            sb.append("(").appendWithSeparator(predicates, ", ").append(") => ")

        sb.append(value)

        return sb.toString()
    }

    fun equals(rhs: Any?): Boolean {
        val other = rhs as? Qualified<T>
        return other != null && value == other.value && predicates == other.predicates
    }

    fun hashCode() = hash(predicates, value)
}
