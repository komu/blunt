package komu.blunt.parser

import java.util.ArrayList

final class IndentStack {
    val indents = arrayList<Int>()

    fun push(column: Int) {
        indents.add(column)
    }

    fun popIf(column: Int): Boolean {
        val last = indents.size - 1
        if (!indents.empty && column <= indents[last]) {
            indents.remove(last)
            return true
        }
        return false
    }

    fun toList(): List<Int> =
        ArrayList(indents)

    fun reset(indents: List<Int>) {
        this.indents.clear()
        this.indents.addAll(indents)
    }
}
