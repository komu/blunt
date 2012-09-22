package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreLetExpression(private val variable: VariableReference,
                        private val value: CoreExpression,
                        private val body: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            instructionsOf(CoreSetExpression(variable, value).assemble(asm, target, Linkage.NEXT))
            instructionsOf(body.assemble(asm, target, linkage))
        }

    override fun simplify() =
        // TODO: if value is constant, propagate it into body
        CoreLetExpression(variable, value.simplify(), body.simplify())

    override fun toString() = "(let ($variable $value) $body)"
}

