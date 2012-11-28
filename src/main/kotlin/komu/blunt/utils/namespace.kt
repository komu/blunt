package komu.blunt.utils

import java.util.ArrayList
import java.util.Collections.emptySet
import java.util.HashSet
import kotlin.nullable.*

fun intersection<T>(sets: Collection<Set<T>>): Set<T> {
    if (sets.empty)
        return emptySet()

    val it = sets.iterator()

    val result = HashSet<T>(it.next())
    while (it.hasNext())
        result.retainAll(it.next())

    return result
}

fun <T> List<T>.concat(rhs: List<T>): List<T> {
    val result = ArrayList<T>(this.size + rhs.size)
    result.addAll(this)
    result.addAll(rhs)
    return result
}

val <T> List<T>.init: List<T>
    get() = if (empty) this else subList(0, size-1)

fun <T> ImmutableArrayListBuilder<T>.addAll(xs: Iterable<T>) {
    for (x in xs)
        add(x)
}

fun StringBuilder.appendTimes(s: String, count: Int): StringBuilder {
    for (i in 1..count)
        append(s)
    return this
}

fun ClassLoader.readResourceAsString(path: String): String? =
    getResourceAsStream(path).map { stream ->
        stream.use { it.reader("UTF-8").readText() }
    }

fun String.contains(ch: Char) = indexOf(ch) != -1
