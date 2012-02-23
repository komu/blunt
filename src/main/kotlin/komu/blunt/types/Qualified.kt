package komu.blunt.types

import std.util.*
import java.util.Collection
import java.util.Collections.emptyList
import java.util.Collections.unmodifiableList
import java.util.ArrayList
import komu.blunt.types.checker.Substitution
import java.util.List
import java.util.LinkedHashSet
import java.util.Objects.hash
import java.util.Set

fun quantifyAll(qt: Qualified<Type>): Scheme {
    val types = LinkedHashSet<TypeVariable?>()
    qt.addTypeVariables(types)
    return quantify(types, qt)
}

fun quantify(vs: Collection<TypeVariable?>, qt: Qualified<Type>): Scheme {
    val kinds = ArrayList<Kind?>()
    val vars = ArrayList<TypeVariable?>()

    val types = LinkedHashSet<TypeVariable?>()
    qt.addTypeVariables(types)

    for (val v in types)
        if (vs.contains(v)) {
            vars.add(v)
            kinds.add(v?.getKind())
        }

    return Scheme(kinds, qt.apply(Substitution.fromTypeVariables(vars)))
}

fun instantiate(ts: List<TypeVariable?>, t: Qualified<Type>): Qualified<Type> {
    val ps = ArrayList<Predicate?>(t.predicates.size())
    for (val p in t.predicates)
        ps.add(p?.instantiate(ts))

    return Qualified(ps, t.value.instantiate(ts))
}

class Qualified<out T : Types<T?>>(predicates: List<Predicate?>, value: T?) : Types<Qualified<T>> {

    val value = value.sure()
    val predicates = unmodifiableList(ArrayList<Predicate?>(predicates)).sure()

    this(value: T?): this(emptyList<Predicate?>().sure(), value)

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        for (val p in predicates)
            p?.addTypeVariables(variables)

        value.addTypeVariables(variables)
    }

    override fun apply(substitution: Substitution?): Qualified<T> =
        Qualified(TypeUtils.applySubstitution(substitution.sure(), predicates), value.apply(substitution))

    override fun toString(): String {
        val sb = StringBuilder()

        if (!predicates.isEmpty()) {
            sb.append("(")

            val it = predicates.iterator().sure()
            while (it.hasNext()) {
                sb.append(it.next())
                if (it.hasNext())
                    sb.append(", ")
            }

            sb.append(") => ")
        }

        sb.append(value)

        return sb.toString().sure()
    }

    override fun equals(rhs: Any?) =
        rhs is Qualified<T> && value == rhs.value && predicates == rhs.predicates

    override fun hashCode() = hash(predicates, value)
}
