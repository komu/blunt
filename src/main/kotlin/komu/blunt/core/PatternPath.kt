package komu.blunt.core

class PatternPath private (private val parent: PatternPath?, private val index: Int) {

    class object {
        val EMPTY = PatternPath(null, -1)
    }

    fun extend(index: Int) = PatternPath(this, index)

    val indices: List<Int>
        get() {
            val indices = listBuilder<Int>()

            var p = this
            while (true) {
                val parent = p.parent ?: break
                indices.add(p.index)
                p = parent
            }

            return indices.build().reverse()
        }

    fun toString() = indices.toString()
}

