package komu.blunt.types

import kotlin.util.*
import java.util.Collections.emptyList
import java.util.Collections.unmodifiableList
import java.util.ArrayList
import komu.blunt.types.checker.Substitution
import java.util.LinkedHashSet
import java.util.Objects.hash
import komu.blunt.types.checker.Substitutions

fun quantifyAll(qt: Qualified<Type>): Scheme {
    val types = LinkedHashSet<TypeVariable>()
    qt.addTypeVariables(types)
    return quantify(types, qt)
}

fun quantify(vs: Collection<TypeVariable>, qt: Qualified<Type>): Scheme {
    val kinds = ArrayList<Kind>()
    val vars = ArrayList<TypeVariable>()

    val types = LinkedHashSet<TypeVariable>()
    qt.addTypeVariables(types)

    for (val v in types)
        if (vs.contains(v)) {
            vars.add(v)
            kinds.add(v.kind)
        }

    return Scheme(kinds, qt.apply(Substitutions.fromTypeVariables(vars)))
}

fun instantiate(ts: List<TypeVariable>, t: Qualified<Type>): Qualified<Type> {
    val ps = ArrayList<Predicate>(t.predicates.size())
    for (val p in t.predicates)
        ps.add(p.instantiate(ts))

    return Qualified(ps, t.value.instantiate(ts))
}

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

        if (!predicates.isEmpty()) {
            sb.append("(")

            val it = predicates.iterator()
            while (it.hasNext()) {
                sb.append(it.next())
                if (it.hasNext())
                    sb.append(", ")
            }

            sb.append(") => ")
        }

        sb.append(value)

        return sb.toString()
    }

    fun equals(rhs: Any?) =
        rhs is Qualified<out Any?> && value == rhs.value && predicates == rhs.predicates

    fun hashCode() = hash(predicates, value)
}
