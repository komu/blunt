package komu.blunt.core

import komu.blunt.asm.*

class CoreLambdaExpression(private val envSize: Int, private val body: CoreExpression) : CoreExpression() {

    init {
        require(envSize >= 0)
    }

    override fun assemble(asm: Assembler, target: Register, linkage: Linkage) =
        instructions {
            // TODO: place the lambda in a new code section
            val lambda = asm.newLabel("lambda")
            val afterLambda = asm.newLabel("after-lambda")

            loadLambda(target, lambda)
            if (linkage == Linkage.Next)
                jump(afterLambda)
            else
                finishWithLinkage(linkage)

            label(lambda)
            createEnvironment(envSize)
            instructionsOf(body.assemble(asm, Register.VAL, Linkage.Return))
            label(afterLambda)
        }

    override fun simplify() = CoreLambdaExpression(envSize, body.simplify())

    override fun toString() = "(lambda $envSize $body)"
}
