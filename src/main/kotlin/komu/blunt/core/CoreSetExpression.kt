package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreSetExpression(private val variable: VariableReference?, private val exp: CoreExpression?) : CoreExpression() {

    override fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val instructions = Instructions()

        instructions.append(exp?.assemble(asm, Register.VAL, Linkage.NEXT).sure())
        instructions.storeVariable(variable, Register.VAL)
        instructions.finishWithLinkage(linkage)

        return instructions
    }

    override fun simplify() = CoreSetExpression(variable, exp?.simplify())
    override fun toString() = "(set! ${variable?.name} $exp)"
}
