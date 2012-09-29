package komu.blunt.utils

import komu.blunt.objects.Symbol

class Sequence(private var value: Int = 1) {
    fun next() = value++
    fun nextSymbol(prefix: String) = Symbol(prefix + next())
}
