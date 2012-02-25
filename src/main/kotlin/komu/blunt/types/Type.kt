package komu.blunt.types

import java.util.LinkedHashSet
import java.util.Set
import java.util.List

abstract class Type : Types<Type?> {
    abstract fun instantiate(vars: List<TypeVariable>): Type
    abstract fun hnf(): Boolean
    abstract val kind: Kind

    fun toString() = toString(0)

    protected abstract fun toString(precedent: Int): String

    fun getTypeVariables(): Set<TypeVariable> {
       val vars = LinkedHashSet<TypeVariable>()
       addTypeVariables(vars)
       return vars
    }

    fun containsVariable(v: TypeVariable): Boolean =
        getTypeVariables().contains(v)
}

