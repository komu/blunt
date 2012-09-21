package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.Assembler
import komu.blunt.asm.Instructions
import komu.blunt.asm.Linkage
import komu.blunt.asm.Register

class CoreLetExpression(private val variable: VariableReference,
                        private val value: CoreExpression,
                        private val body: CoreExpression) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.append(CoreSetExpression(variable, value).assemble(asm, target, Linkage.NEXT))
        instructions.append(body.assemble(asm, target, linkage))
        return instructions
    }

    override fun simplify() =
        // TODO: if value is constant, propagate it into body
        CoreLetExpression(variable, value.simplify(), body.simplify())

    override fun toString() = "(let ($variable $value) $body)"
}

