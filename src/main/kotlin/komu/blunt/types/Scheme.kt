package komu.blunt.types

import java.util.Objects
import komu.blunt.types.checker.Substitution

class Scheme(val kinds: List<Kind>, val `type`: Qualified<Type>) : Types<Scheme> {

    override fun apply(substitution: Substitution): Scheme =
        Scheme(kinds, `type`.apply(substitution))

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        `type`.addTypeVariables(result)
    }

    fun toString() = `type`.toString()

    fun equals(obj: Any?) = obj is Scheme && kinds == obj.kinds && `type` == obj.`type`
    fun hashCode() = Objects.hash(kinds, `type`)
}
