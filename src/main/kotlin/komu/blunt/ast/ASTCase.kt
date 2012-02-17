package komu.blunt.ast

import com.google.common.collect.ImmutableList
import java.util.ArrayList

class ASTCase(val exp: ASTExpression, val alternatives: ImmutableList<ASTAlternative?>) : ASTExpression() {

    override fun toString() = "case $exp of $alternatives"

    override fun simplify(): ASTExpression {
        val alts = ArrayList<ASTAlternative?>()

        for (val alt in alternatives)
            alts.add(alt?.simplify())

        return ASTCase(exp.simplify().sure(), ImmutableList.copyOf(alts).sure())
    }
}
