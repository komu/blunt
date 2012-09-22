package komu.blunt.core

import komu.blunt.asm.*

abstract class CoreExpression {

    abstract fun simplify(): CoreExpression
    abstract fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions
    open fun toString(): String = "<core-expression>"

    class object {

        fun and(exps: List<CoreExpression>): CoreExpression {
            return if (exps.empty)
                CoreConstantExpression.TRUE
            else if (exps.size == 1)
                exps.first()
            else
                CoreIfExpression(exps.first(), and(exps.tail), CoreConstantExpression.FALSE)
        }
    }
}

