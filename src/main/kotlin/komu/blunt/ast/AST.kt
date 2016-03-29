package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorNames
import komu.blunt.types.patterns.Pattern

/**
 * Convenience functions for constructing syntax objects.
 */
object AST {

    fun apply(func: ASTExpression, vararg args: ASTExpression): ASTExpression =
        args.fold(func) { exp, arg -> ASTExpression.Application(exp, arg) }.simplify()

    fun error(message: String): ASTExpression =
        AST.apply(ASTExpression.Variable("error"), ASTExpression.Constant(message))

    fun constructor(name: String, vararg args: ASTExpression): ASTExpression =
        apply(ASTExpression.Constructor(name), *args)

    fun lambda(arguments: List<Symbol>, body: ASTExpression): ASTExpression {
        require(arguments.any()) { "no arguments for lambda" }

        val head = arguments.first()
        val tail = if (arguments.size == 1) body else lambda(arguments.drop(1), body)
        return ASTExpression.Lambda(head, tail)
    }

    fun ifExp(test: ASTExpression, cons: ASTExpression, alt: ASTExpression): ASTExpression =
        ASTExpression.Case(test,
                    ASTAlternative(Pattern.Constructor(ConstructorNames.TRUE), cons),
                    ASTAlternative(Pattern.Constructor(ConstructorNames.FALSE), alt))
}
