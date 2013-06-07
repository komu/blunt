package komu.blunt.core

import java.util.Collections.emptyList
import komu.blunt.asm.*

abstract class CoreExpression {

    abstract fun simplify(): CoreExpression
    abstract fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions
    open fun toString(): String = "<core-expression>"

    class object {

        val TRUE  = CoreConstantExpression(true)
        val FALSE = CoreConstantExpression(false)
        val EMPTY = CoreSequenceExpression(emptyList())

        fun sequence(vararg exps: CoreExpression) = sequence(exps.toList())

        fun sequence(exps: List<CoreExpression>): CoreExpression =
            if (exps.size == 1) exps.first() else CoreSequenceExpression(exps)

        fun and(exps: List<CoreExpression>): CoreExpression =
            when (exps.size) {
                0    -> TRUE
                1    -> exps.first()
                else -> CoreIfExpression(exps.first(), and(exps.tail), FALSE)
            }
    }
}

