package komu.blunt.core

import com.google.common.collect.Lists

import java.util.ArrayList

class PatternPath private (private val parent: PatternPath?, private val index: Int) {

    class object {
        val EMPTY = PatternPath(null, -1)
    }

    fun extend(index: Int) = PatternPath(this, index)

    fun indices(): List<Int> {
        val indices = ArrayList<Int>()

        var p = this
        while (true) {
            indices.add(p.index)

            if (p.parent != null)
                p = p.parent!!
            else
                break;
        }

        return Lists.reverse(indices)
    }

    fun toString() = indices().toString()
}

