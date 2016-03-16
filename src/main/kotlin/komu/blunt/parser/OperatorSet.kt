package komu.blunt.parser

import java.lang.Math.max
import java.util.*

class OperatorSet() {

    private val ops = HashMap<String,Operator>()
    private var maxPrecedence = 0
    private val DEFAULT_PRECEDENCE = 0

    init {
        add(1, Associativity.RIGHT, "$")
        add(2, Associativity.RIGHT, ":")
        add(3, "==", "<", "<=", ">", ">=")
        add(4, "+", "-")
        add(5, "*", "/", "%")
    }

    fun add(precedence: Int, vararg names: String) {
        addInternal(precedence, Associativity.LEFT, names)
    }

    fun add(precedence: Int, associativity: Associativity, vararg names: String) {
        addInternal(precedence, associativity, names)
    }

    fun addInternal(precedence: Int, associativity: Associativity, names: Array<out String>) {
        require(precedence >= 0)

        maxPrecedence = max(maxPrecedence, precedence)
        for (name in names)
            ops[name] = Operator(name, associativity, precedence)
    }

    operator fun get(name: String): Operator =
        ops[name] ?: Operator(name, Associativity.LEFT, DEFAULT_PRECEDENCE)

    val maxLevel: Int
        get() = maxPrecedence
}
