package komu.blunt.types

import com.google.common.base.Strings.repeat

object ConstructorNames {

    val CONS  = ":"
    val NIL   = "[]"
    val UNIT  = "()"
    val TRUE  = "True"
    val FALSE = "False"

    fun tupleName(arity: Int) = "(" + repeat(",", arity-1) + ")"
}

