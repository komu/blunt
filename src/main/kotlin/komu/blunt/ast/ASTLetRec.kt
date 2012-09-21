package komu.blunt.ast

class ASTLetRec(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    // TODO: convert letrecs to lets if variable is not referenced in binding
    override fun simplify() =
        ASTLetRec(bindings.map { it.simplify() }, body.simplify())

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(letrec (")

        val it = bindings.iterator()
        while (it.hasNext()) {
            sb.append(it.next())
            if (it.hasNext())
                sb.append(' ')
        }
        sb.append(") ")
        sb.append(body)
        sb.append(')')

        return sb.toString()
    }
}
