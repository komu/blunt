package komu.blunt.ast

import com.google.common.collect.ImmutableList
import java.util.ArrayList

class ASTSequence(val exps: ImmutableList<ASTExpression?>) : ASTExpression() {

    fun last(): ASTExpression =
        exps.get(exps.size()-1).sure()

    fun allButLast(): ImmutableList<ASTExpression?> =
        exps.subList(0, exps.size()-1).sure()

    override fun simplify(): ASTExpression {
        val result = ArrayList<ASTExpression?>()

        for (val exp in exps)
            result.add(exp?.simplify())

        if (result.size() == 1)
            return result.get(0).sure()
        else
            return ASTSequence(ImmutableList.copyOf(result).sure())
    }

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("(begin")

        for (val exp in exps)
            sb.append(' ')?.append(exp)

        sb.append(')')

        return sb.toString().sure()
    }
}

