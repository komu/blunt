package komu.blunt.types

import java.util.Objects
import komu.blunt.types.checker.Substitution

class Scheme(val kinds: List<Kind>, val `type`: Qualified<Type>) : Types<Scheme> {

    override fun apply(substitution: Substitution): Scheme =
        Scheme(kinds, `type`.apply(substitution))

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        `type`.addTypeVariables(result)
    }

    override fun toString() = `type`.toString()

    override fun equals(other: Any?) = other is Scheme && kinds == other.kinds && `type` == other.`type`
    override fun hashCode() = Objects.hash(kinds, `type`)
}
