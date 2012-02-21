package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreExtractTagExpression(private val variable: VariableReference, private val path: PatternPath) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.loadVariable(target, variable)
        instructions.loadTag(target, target, path)
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun simplify() = this
    override fun toString() = "(extract-tag ${variable.name} $path)"
}
