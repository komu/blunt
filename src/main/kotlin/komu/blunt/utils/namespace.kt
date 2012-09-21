package komu.blunt.utils

import java.util.Collections.emptySet
import java.util.HashSet

fun intersection<T>(sets: Collection<Set<T>>): Set<T> {
    if (sets.isEmpty())
        return emptySet()

    val it = sets.iterator()

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}
