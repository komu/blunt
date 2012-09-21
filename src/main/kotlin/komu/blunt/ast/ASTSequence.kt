package komu.blunt.ast

import java.util.ArrayList
import com.google.common.collect.ImmutableList

class ASTSequence(val exps: List<ASTExpression>) : ASTExpression() {

    fun last(): ASTExpression =
        exps.get(exps.size()-1)

    fun allButLast(): List<ASTExpression> =
        exps.subList(0, exps.size()-1)

    override fun simplify(): ASTExpression {
        val result = ArrayList<ASTExpression>()

        for (val exp in exps)
            result.add(exp.simplify())

        if (result.size() == 1)
            return result[0]
        else
            return ASTSequence(ImmutableList.copyOf(result))
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("(begin")

        for (val exp in exps)
            sb.append(' ').append(exp)

        sb.append(')')

        return sb.toString()
    }
}

