package komu.blunt.utils

import java.util.Collection
import java.util.HashSet
import java.util.Iterator
import java.util.Set

import java.util.Collections.emptySet

fun intersection<T>(sets: Collection<Set<T>>): Set<T> {
    val it = sets.iterator().sure()

    if (!it.hasNext())
        return emptySet<T>().sure()

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}
