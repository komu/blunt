package komu.blunt.core

import com.google.common.collect.Lists

class PatternPath private (private val parent: PatternPath?, private val index: Int) {

    class object {
        val EMPTY = PatternPath(null, -1)
    }

    fun extend(index: Int) = PatternPath(this, index)

    fun indices(): List<Int> {
        val indices = listBuilder<Int>()

        var p = this
        while (true) {
            val parent = p.parent
            if (parent != null) {
                indices.add(p.index)
                p = parent
            } else
                break
        }

        return Lists.reverse(indices.build())
    }

    fun toString() = indices().toString()
}

