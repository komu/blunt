package komu.blunt.ast

import com.google.common.collect.ImmutableList

import java.util.ArrayList

class ASTLetRec(val bindings: ImmutableList<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

    override fun simplify(): ASTExpression {
        // TODO: convert letrecs to lets if variable is not referenced in binding
        val simplifiedBindings = ArrayList<ImplicitBinding>()
        for (val binding in bindings)
            simplifiedBindings.add(binding.simplify())
        return ASTLetRec(ImmutableList.copyOf(simplifiedBindings).sure(), body.simplify().sure())
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("(letrec (")

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
