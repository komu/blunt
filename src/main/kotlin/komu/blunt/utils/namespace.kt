package komu.blunt.utils

import java.util.Collections.emptySet
import java.util.HashSet

fun intersection<T>(sets: Collection<Set<T>>): Set<T> {
    if (sets.empty)
        return emptySet()

    val it = sets.iterator()

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}

fun <T> List<T>.allButLast(): List<T> =
    subList(0, size-1)

fun StringBuilder.appendWithSeparator(xs: Iterable<Any?>, separator: String): StringBuilder {
    var first = true

    for (x in xs) {
        if (first)
            first = false
        else
            append(separator)

        append(x)
    }
    return this
}
