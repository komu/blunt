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

val <T> List<T>.init: List<T>
    get() = if (empty) this else subList(0, size-1)

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


fun StringBuilder.appendWithSeparator(xs: Array<Any?>, separator: String): StringBuilder {
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


fun StringBuilder.appendTimes(s: String, count: Int): StringBuilder {
    for (i in 1..count)
        sb.append(s)
    return this
}

fun ClassLoader.readResourceAsString(path: String): String? {
    val stream = javaClass.getClassLoader().getResourceAsStream(path)
    return if (stream != null)
        stream.use { it.reader("UTF-8").readText() }
    else
        null
}
