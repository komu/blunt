package komu.blunt.ast

class ASTSequence(val exps: List<ASTExpression>) : ASTExpression() {

    fun last(): ASTExpression =
        exps.last()

    fun allButLast(): List<ASTExpression> =
        exps.subList(0, exps.size-1)

    override fun simplify(): ASTExpression {
        val simplified = exps.map { it.simplify() }
        return if (simplified.size == 1) simplified.first() else ASTSequence(simplified)
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("(begin")

        for (val exp in exps)
            sb.append(' ').append(exp)

        sb.append(')')

        return sb.toString()
    }
}
