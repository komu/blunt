package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreVariableExpression(private val variable: VariableReference) : CoreExpression() {

    override fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val instructions = Instructions()
        instructions.loadVariable(target, variable)
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun simplify() = this
    override fun toString() = variable.name.toString().sure()
}
