package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*
import komu.blunt.stdlib.BasicValues

class CoreDefineExpression(private val variable: VariableReference?, private val expression: CoreExpression?) : CoreExpression() {

    override fun assemble(asm: Assembler?, target: Register?, linkage: Linkage?): Instructions {
        val instructions = Instructions()

        instructions.append(expression?.assemble(asm, target, Linkage.NEXT))

        instructions.storeVariable(variable, target)

        instructions.loadConstant(target, BasicValues.UNIT)
        instructions.finishWithLinkage(linkage)

        return instructions
    }

    override fun simplify() = CoreDefineExpression(variable, expression?.simplify())
}

