package komu.blunt.core

import komu.blunt.asm.*

class CoreSequenceExpression(private val expressions: List<CoreExpression>) : CoreExpression() {

    class object {
        fun of(vararg expressions: CoreExpression) = CoreSequenceExpression(expressions.toList())
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()

        if (!expressions.isEmpty()) {
            for (val exp in allButLast())

            instructions.append(last().assemble(asm, target, linkage))
        } else {
            instructions.finishWithLinkage(linkage)
        }

        return instructions
    }

    private fun last(): CoreExpression =
        expressions[expressions.size()-1]

    private fun allButLast(): List<CoreExpression> =
        expressions.subList(0, expressions.size()-1)

    override fun toString() = expressions.toString()

    override fun simplify(): CoreExpression {
        val exps = arrayList<CoreExpression>()
        for (val exp in expressions)
            if (exp != CoreEmptyExpression.INSTANCE)
                exps.add(exp.simplify())

        if (exps.isEmpty())
            return CoreEmptyExpression.INSTANCE
        else if (exps.size == 1)
            return exps.first()
        else
            return CoreSequenceExpression(exps)
    }
}
