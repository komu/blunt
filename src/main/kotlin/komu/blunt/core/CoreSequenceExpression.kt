package komu.blunt.core

import komu.blunt.asm.*
import komu.blunt.utils.allButLast

class CoreSequenceExpression (private val expressions: List<CoreExpression>) : CoreExpression() {

    class object {
        fun of(vararg expressions: CoreExpression) = CoreSequenceExpression(expressions.toList())
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()

        if (!expressions.empty) {
            for (exp in expressions.allButLast())
                instructions.append(exp.assemble(asm, target, Linkage.NEXT))

            instructions.append(expressions.last().assemble(asm, target, linkage))
        } else {
            instructions.finishWithLinkage(linkage)
        }

        return instructions
    }

    override fun toString() = "(begin $expressions)"

    override fun simplify(): CoreExpression {
        val exps = arrayList<CoreExpression>()
        for (exp in expressions)
            if (exp != CoreEmptyExpression.INSTANCE)
                exps.add(exp.simplify())

        return if (exps.empty)
            CoreEmptyExpression.INSTANCE
        else if (exps.size == 1)
            exps.first()
        else
            CoreSequenceExpression(exps)
    }
}
