package komu.blunt.ast

class ASTSequence(val exps: List<ASTExpression>) : ASTExpression() {

    fun map(f: (ASTExpression) -> ASTExpression) = ASTSequence(exps.map(f))

    override fun simplify(): ASTExpression {
        val simplified = exps.map { it.simplify() }
        return if (simplified.size == 1) simplified.first() else ASTSequence(simplified)
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("(begin")

        for (exp in exps)
            sb.append(' ').append(exp)

        sb.append(')')

        return sb.toString()
    }
}
