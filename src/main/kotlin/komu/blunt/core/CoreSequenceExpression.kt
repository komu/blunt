package komu.blunt.core

import komu.blunt.asm.*

import java.util.ArrayList
import java.util.List

import java.util.Arrays
import java.util.Collections

class CoreSequenceExpression(expressions: List<CoreExpression>) : CoreExpression() {

    private val expressions = ArrayList<CoreExpression>(expressions)

    this(vararg expressions: CoreExpression): this(Collections.emptyList<CoreExpression>().sure()) {
        throw UnsupportedOperationException("construct list from varargs") // TODO
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
        expressions.subList(0, expressions.size()-1).sure()

    override fun toString() = expressions.toString().sure()

    override fun simplify(): CoreExpression {
        val exps = ArrayList<CoreExpression>(expressions.size());
        for (val exp in expressions)
            if (exp != CoreEmptyExpression.INSTANCE)
                exps.add(exp.simplify())

        if (exps.isEmpty())
            return CoreEmptyExpression.INSTANCE
        else if (exps.size() == 1)
            return exps[0]
        else
            return CoreSequenceExpression(exps)
    }
}
