package komu.blunt.ast

import com.google.common.collect.ImmutableList

import java.util.ArrayList

class ASTLet(val bindings: ImmutableList<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    override fun simplify(): ASTExpression {
        val simplifiedBindings = ArrayList<ImplicitBinding>()
        for (val binding in bindings)
            simplifiedBindings.add(binding.simplify())
        return ASTLet(ImmutableList.copyOf(simplifiedBindings).sure(), body.simplify())
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(let (")

        val it = bindings.iterator().sure()
        while (it.hasNext()) {
            sb.append(it.next())
            if (it.hasNext())
                sb.append(' ')
        }
        sb.append(") ")
        sb.append(body)
        sb.append(')')

        return sb.toString().sure()
    }
}
