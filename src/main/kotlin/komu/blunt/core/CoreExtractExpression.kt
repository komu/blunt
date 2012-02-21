package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreExtractExpression(private val variable: VariableReference?, private val path: PatternPath?) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage): Instructions {
        val instructions = Instructions()
        instructions.loadVariable(target, variable)
        instructions.loadExtracted(target, target, path)
        instructions.finishWithLinkage(linkage)
        return instructions
    }

    override fun simplify() = this
    override fun toString() = "(extract $path)"
}

