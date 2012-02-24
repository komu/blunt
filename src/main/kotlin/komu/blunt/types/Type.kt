package komu.blunt.types

import java.util.LinkedHashSet
import java.util.Set

fun Type.getTypeVariables(): Set<TypeVariable?> {
    val vars = LinkedHashSet<TypeVariable?>()
    addTypeVariables(vars)
    return vars

}

fun Type.containsVariable(v: TypeVariable): Boolean =
    getTypeVariables().contains(v)
