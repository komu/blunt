package komu.blunt.types

import komu.blunt.utils.appendTimes

object ConstructorNames {

    public val CONS: String  = ":"
    public val NIL: String   = "[]"
    public val UNIT: String  = "()"
    public val TRUE: String  = "True"
    public val FALSE: String = "False"

    fun tupleName(arity: Int) = StringBuilder("(").appendTimes(",", arity-1).append(")").toString()
}

