package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreSetExpression(private val variable: VariableReference, private val exp: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            instructionsOf(exp.assemble(asm, Register.VAL, Linkage.NEXT))
            storeVariable(variable, Register.VAL)
            finishWithLinkage(linkage)
        }

    override fun simplify() = CoreSetExpression(variable, exp.simplify())
    override fun toString() = "(set! ${variable.name} $exp)"
}
