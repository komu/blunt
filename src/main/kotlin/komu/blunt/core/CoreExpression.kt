package komu.blunt.core

import komu.blunt.asm.Assembler
import komu.blunt.asm.Instructions
import komu.blunt.asm.Linkage
import komu.blunt.asm.Register
import java.util.Collections.emptyList

abstract class CoreExpression {

    abstract fun simplify(): CoreExpression
    abstract fun assemble(asm: Assembler, target: Register, linkage: Linkage = Linkage.Next): Instructions
    override fun toString(): String = "<core-expression>"

    companion object {

        val TRUE  = CoreConstantExpression(true)
        val FALSE = CoreConstantExpression(false)
        val EMPTY = CoreSequenceExpression(emptyList())

        fun sequence(vararg exps: CoreExpression) = sequence(exps.asList())

        fun sequence(exps: List<CoreExpression>): CoreExpression =
            if (exps.size == 1) exps.first() else CoreSequenceExpression(exps)

        fun and(exps: List<CoreExpression>): CoreExpression =
            when (exps.size) {
                0    -> TRUE
                1    -> exps.first()
                else -> CoreIfExpression(exps.first(), and(exps.drop(1)), FALSE)
            }
    }
}

