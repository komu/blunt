package komu.blunt.core

import komu.blunt.asm.Assembler
import komu.blunt.asm.Instructions
import komu.blunt.asm.Linkage
import komu.blunt.asm.Register

import java.util.ArrayList
import java.util.List

import java.util.Arrays

class CoreSequenceExpression(expressions: List<CoreExpression?>?) : CoreExpression() {

    private val expressions = ArrayList<CoreExpression?>(expressions.sure())

    this(vararg expressions: CoreExpression): this(null) {
        throw UnsupportedOperationException("construct list from varargs") // TODO
    }

    override fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions {
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
        expressions.get(expressions.size()-1).sure()

    private fun allButLast(): List<CoreExpression?> =
        expressions.subList(0, expressions.size()-1).sure()

    override fun toString() = expressions.toString()

    override fun simplify(): CoreExpression {
        val exps = ArrayList<CoreExpression?>(expressions.size());
        for (val exp in expressions)
            if (exp != CoreEmptyExpression.INSTANCE)
                exps.add(exp?.simplify())

        if (exps.isEmpty())
            return CoreEmptyExpression.INSTANCE.sure()
        else if (exps.size() == 1)
            return exps.get(0).sure()
        else
            return CoreSequenceExpression(exps)
    }
}

