package komu.blunt.parser

import java.util.HashMap
import java.util.Map

import com.google.common.base.Preconditions.checkArgument
import java.lang.Math.max

class OperatorSet() {

    private val ops = HashMap<String,Operator>()
    private var maxPrecedence = 0
    private val DEFAULT_PRECEDENCE = 0

    {
        add(1, Associativity.RIGHT, "$")
        add(2, Associativity.RIGHT, ":")
        add(3, "==", "<", "<=", ">", ">=")
        add(4, "+", "-")
        add(5, "*", "/", "%")
    }

    fun add(precedence: Int, vararg names: String) {
        addInternal(precedence, Associativity.LEFT, names);
    }

    fun add(precedence: Int, associativity: Associativity?, vararg names: String) {
        addInternal(precedence, associativity, names)
    }

    fun addInternal(precedence: Int, associativity: Associativity?, names: Array<String>) {
        checkArgument(precedence >= 0);

        maxPrecedence = max(maxPrecedence, precedence);
        for (val name in names)
            ops.put(name, Operator(name, associativity.sure(), precedence))
    }

    fun get(name: String): Operator {
        val op = ops.get(name)
        if (op != null)
            return op;
        else
            return Operator(name, Associativity.LEFT.sure(), DEFAULT_PRECEDENCE);
    }

    val maxLevel: Int
        get() = maxPrecedence
}
