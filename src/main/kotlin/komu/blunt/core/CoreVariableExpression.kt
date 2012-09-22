package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreVariableExpression(private val variable: VariableReference) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            loadVariable(target, variable)
            finishWithLinkage(linkage)
        }

    override fun simplify() = this
    override fun toString() = variable.name.toString()
}
