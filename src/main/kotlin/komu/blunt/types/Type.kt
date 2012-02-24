package komu.blunt.types

import java.util.LinkedHashSet
import java.util.Set
import java.util.List

abstract class Type : Types<Type?> {
    abstract fun instantiate(vars: List<TypeVariable?>?): Type
    abstract fun getKind(): Kind?
    abstract fun hnf(): Boolean

    fun toString() = toString(0)

    protected abstract fun toString(precedent: Int): String
}


fun Type.getTypeVariables(): Set<TypeVariable?> {
    val vars = LinkedHashSet<TypeVariable?>()
    addTypeVariables(vars)
    return vars

}

fun Type.containsVariable(v: TypeVariable): Boolean =
    getTypeVariables().contains(v)
