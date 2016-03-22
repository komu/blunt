package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.ConstructorNames
import komu.blunt.types.Type
import komu.blunt.types.patterns.Pattern

/**
 * Convenience functions for constructing syntax objects.
 */
object AST {

    fun data(name: String, typ: Type, constructors: List<ConstructorDefinition>, derivedClasses: List<String>) =
        ASTDataDefinition(name, typ, constructors, derivedClasses)

    fun apply(func: ASTExpression, vararg args: ASTExpression): ASTExpression =
        apply2(func, args)

    private fun apply2(func: ASTExpression, args: Array<out ASTExpression>): ASTExpression {
        var exp = func

        for (arg in args)
            exp = ASTExpression.Application(exp, arg)

        return exp.simplify()
    }

    fun error(message: String): ASTExpression =
        AST.apply(ASTExpression.Variable("error"), ASTExpression.Constant(message))

    fun constructor(name: String, vararg args: ASTExpression): ASTExpression =
        apply2(ASTExpression.Constructor(name), args)

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

    fun let(recursive: Boolean, binding: ImplicitBinding, body: ASTExpression): ASTExpression {
        val bindings = listOf(binding)
        return if (recursive) ASTExpression.LetRec(bindings, body) else ASTExpression.Let(bindings, body)
    }
}
