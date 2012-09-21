package komu.blunt.types

object ConstructorNames {

    public val CONS: String  = ":"
    public val NIL: String   = "[]"
    public val UNIT: String  = "()"
    public val TRUE: String  = "True"
    public val FALSE: String = "False"

    fun tupleName(arity: Int) = "(" + repeat(",", arity-1) + ")"

    private fun repeat(s: String, count: Int): String {
        val sb = StringBuilder(s.length * count)
        for (i in 1..count)
            sb.append(s)
        return sb.toString()
    }
}

