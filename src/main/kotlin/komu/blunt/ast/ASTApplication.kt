package komu.blunt.ast

import com.google.common.collect.ImmutableList

class ASTApplication(val func: ASTExpression, val arg: ASTExpression) : ASTExpression() {

    override fun toString() = "($func $arg)"

    override fun simplify(): ASTExpression {
        val simplifiedFunc = func.simplify().sure()
        val simplifiedArg = arg.simplify().sure()

        if (simplifiedFunc is ASTLambda) {
            val bindings = ImmutableList.of<ImplicitBinding?>(ImplicitBinding(simplifiedFunc.argument, simplifiedArg))
            return ASTLet(bindings, simplifiedFunc.body).simplify().sure()
        } else
            return ASTApplication(simplifiedFunc, simplifiedArg)
    }
}

