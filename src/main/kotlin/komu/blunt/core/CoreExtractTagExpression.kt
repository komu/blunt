package komu.blunt.core

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*

class CoreExtractTagExpression(private val variable: VariableReference, private val path: PatternPath) : CoreExpression() {

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            loadVariable(target, variable)
            loadTag(target, target, path)
            finishWithLinkage(linkage)
        }

    override fun simplify() = this
    override fun toString() = "(extract-tag ${variable.name} $path)"
}
