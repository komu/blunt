package komu.blunt.core

import komu.blunt.asm.Assembler
import komu.blunt.asm.Instructions
import komu.blunt.asm.Linkage
import komu.blunt.asm.Register

import java.util.List

abstract class CoreExpression {

    abstract fun simplify(): CoreExpression
    abstract fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions
    open fun toString(): String = "<core-expression>"

    class object {

        fun and(exps: List<CoreExpression?>): CoreExpression {
            return if (exps.isEmpty())
                CoreConstantExpression(true)
            else if (exps.size() == 1)
                exps.get(0).sure()
            else
                CoreIfExpression(exps.get(0), and(exps.subList(1, exps.size()).sure()), CoreConstantExpression(false))
        }
    }
}

