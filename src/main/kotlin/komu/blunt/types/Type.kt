package komu.blunt.types

import java.util.Collections.emptyList

abstract class Type : Types<Type> {
    abstract fun instantiate(vars: List<TypeVariable>): Type
    abstract val hnf: Boolean
    abstract val kind: Kind

    fun toString() = toString(0)
    fun toScheme() = Scheme(emptyList(), Qualified.simple(this))

    protected abstract fun toString(precedence: Int): String
}

fun List<Type>.kinds() = map { it.kind }

