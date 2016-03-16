package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.utils.init
import java.util.*

class CoreSequenceExpression (private val expressions: List<CoreExpression>) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            if (expressions.any()) {
                for (exp in expressions.init)
                    instructionsOf(exp.assemble(asm, target, Linkage.NEXT))

                instructionsOf(expressions.last().assemble(asm, target, linkage))
            } else {
                finishWithLinkage(linkage)
            }
        }

    override fun toString() = "(begin $expressions)"

    override fun simplify(): CoreExpression {
        val exps = ArrayList<CoreExpression>()
        for (exp in expressions) {
            val sexp = exp.simplify()
            if (sexp is CoreSequenceExpression)
                exps.addAll(sexp.expressions)
            else
                exps.add(sexp)
        }

        return CoreExpression.sequence(exps)
    }
}
