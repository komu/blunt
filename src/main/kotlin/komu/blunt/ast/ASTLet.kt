package komu.blunt.ast

class ASTLet(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    override fun simplify(): ASTExpression =
        ASTLet(bindings.map { it.simplify() }, body.simplify())

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(let (")

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
