package komu.blunt.utils

import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Collections.emptySet

fun <T> intersection(sets: Collection<Set<T>>): Set<T> {
    if (sets.isEmpty())
        return emptySet()

    val it = sets.iterator()

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}

val <T> List<T>.init: List<T>
    get() = if (isEmpty()) this else subList(0, size-1)

fun StringBuilder.appendTimes(s: String, count: Int): StringBuilder {
    for (i in 1..count)
        append(s)
    return this
}

fun ClassLoader.readResourceAsString(path: String): String? =
    getResourceAsStream(path)?.use { it.reader(StandardCharsets.UTF_8).readText() }
