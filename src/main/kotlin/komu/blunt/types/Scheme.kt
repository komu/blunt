package komu.blunt.types

import kotlin.util.*

import komu.blunt.types.checker.Substitution

import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.Set
import java.util.Objects

class Scheme(val kinds: List<Kind?>, val `type`: Qualified<Type>) : Types<Scheme> {

    override fun apply(substitution: Substitution): Scheme =
        Scheme(kinds, `type`.apply(substitution))

    override fun addTypeVariables(variables: Set<TypeVariable>) {
        `type`.addTypeVariables(variables)
    }

    fun toString() = `type`.toString()

    fun equals(obj: Any?) = obj is Scheme && kinds == obj.kinds && `type` == obj.`type`
    fun hashCode() = Objects.hash(kinds, `type`)

    class object {
        fun fromType(t: Type) = Scheme(Collections.emptyList<Kind?>().sure(), Qualified(t))

        fun fromTypes(ts: List<out Type>): List<Scheme> {
            val schemes = ArrayList<Scheme>(ts.size)
            for (val t in ts)
                schemes.add(Scheme.fromType(t))
            return schemes
        }
    }
}
