package komu.blunt.utils

import java.util.HashSet

import java.util.Collections.emptySet

fun intersection<T>(sets: Collection<Set<T>>): Set<T> {
    val it = sets.iterator()

    if (!it.hasNext())
        return emptySet<T>()!!

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}
