package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.Set

class Scheme(val kinds: List<Kind?>?, val `type`: Qualified<Type>?) : Types<Scheme> {

    override fun apply(substitution: Substitution?): Scheme =
        Scheme(kinds, `type`?.apply(substitution))

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        `type`?.addTypeVariables(variables)
    }

    override fun toString() = `type`.toString()

    override fun equals(obj: Any?) = obj is Scheme && kinds == obj.kinds && `type` == obj.`type`
    override fun hashCode() = kinds.sure().hashCode() * 79 + `type`.sure().hashCode()

    class object {
        fun fromType(t: Type?) = Scheme(Collections.emptyList<Kind?>, Qualified(t))

        fun fromTypes(ts: List<out Type?>?): List<Scheme?> {
            val schemes = ArrayList<Scheme?>(ts.sure().size())
            for (val t in ts)
                schemes.add(Scheme.fromType(t))
            return schemes
        }
    }
}
