package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.utils.addAll
import komu.blunt.utils.init

class CoreSequenceExpression (private val expressions: List<CoreExpression>) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            if (!expressions.empty) {
                for (exp in expressions.init)
                    instructionsOf(exp.assemble(asm, target, Linkage.NEXT))

                instructionsOf(expressions.last().assemble(asm, target, linkage))
            } else {
                finishWithLinkage(linkage)
            }
        }

    override fun toString() = "(begin $expressions)"

    override fun simplify(): CoreExpression {
        val builder = listBuilder<CoreExpression>()
        for (exp in expressions) {
            val sexp = exp.simplify()
            if (sexp is CoreSequenceExpression)
                builder.addAll(sexp.expressions)
            else
                builder.add(sexp)
        }

        return CoreExpression.sequence(builder.build())
    }
}
