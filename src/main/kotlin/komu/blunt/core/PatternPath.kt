package komu.blunt.core

import java.util.*

class PatternPath private constructor(private val parent: PatternPath?, private val index: Int) {

    companion object {
        val EMPTY = PatternPath(null, -1)
    }

    fun extend(index: Int) = PatternPath(this, index)

    val indices: List<Int>
        get() {
            val indices = ArrayList<Int>()

            var p = this
            while (true) {
                val parent = p.parent ?: break
                indices.add(p.index)
                p = parent
            }

            indices.reverse()
            return indices
        }

    override fun toString() = indices.toString()
}

