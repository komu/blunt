package komu.blunt.types

import com.google.common.base.Strings.repeat

object ConstructorNames {

    public val CONS: String  = ":"
    public val NIL: String   = "[]"
    public val UNIT: String  = "()"
    public val TRUE: String  = "True"
    public val FALSE: String = "False"

    fun tupleName(arity: Int) = "(" + repeat(",", arity-1) + ")"
}

