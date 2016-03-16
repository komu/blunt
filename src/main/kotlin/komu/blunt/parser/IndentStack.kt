package komu.blunt.parser

import java.util.*

final class IndentStack {
    private val indents = ArrayList<Int>()

    fun push(column: Int) {
        indents.add(column)
    }

    fun popIf(column: Int): Boolean =
        if (indents.any() && column <= indents.last()) {
            indents.removeAt(indents.lastIndex)
            true
        } else {
            false
        }

    fun toList(): List<Int> =
        ArrayList(indents)

    fun reset(indents: List<Int>) {
        this.indents.clear()
        this.indents.addAll(indents)
    }
}
