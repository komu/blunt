package komu.blunt.core

import java.util.Collections.emptyList
import komu.blunt.asm.*

abstract class CoreExpression {

    abstract fun simplify(): CoreExpression
    abstract fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions
    open fun toString(): String = "<core-expression>"

    class object {

        val EMPTY = CoreSequenceExpression(emptyList())

        fun and(exps: List<CoreExpression>): CoreExpression =
            when (exps.size) {
                0 -> CoreConstantExpression.TRUE
                1 -> exps.first()
                else -> CoreIfExpression(exps.first(), and(exps.tail), CoreConstantExpression.FALSE)
            }
    }
}

