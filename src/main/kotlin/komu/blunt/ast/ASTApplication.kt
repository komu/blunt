package komu.blunt.ast

import java.util.Collections.singletonList

class ASTApplication(val func: ASTExpression, val arg: ASTExpression) : ASTExpression() {

    override fun toString() = "($func $arg)"

    override fun simplify(): ASTExpression {
        val simplifiedFunc = func.simplify()
        val simplifiedArg = arg.simplify()

        if (simplifiedFunc is ASTLambda) {
            val bindings = singletonList(ImplicitBinding(simplifiedFunc.argument, simplifiedArg))
            return ASTLet(bindings, simplifiedFunc.body).simplify()
        } else
            return ASTApplication(simplifiedFunc, simplifiedArg)
    }
}

