package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*
import komu.blunt.stdlib.BasicValues

class CoreDefineExpression(private val variable: VariableReference, private val expression: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            instructionsOf(expression.assemble(asm, target, Linkage.Next))
            storeVariable(variable, target)

            loadConstant(target, BasicValues.UNIT)
            finishWithLinkage(linkage)
        }

    override fun simplify() = CoreDefineExpression(variable, expression.simplify())

    override fun toString() = "(define $variable $expression)"
}

