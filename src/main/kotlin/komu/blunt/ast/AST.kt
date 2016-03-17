package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.ConstructorNames
import komu.blunt.types.Type
import komu.blunt.types.patterns.Pattern
import java.util.Collections.singletonList

/**
 * Convenience functions for constructing syntax objects.
 */
object AST {

    fun data(name: String, typ: Type, constructors: List<ConstructorDefinition>, derivedClasses: List<String>) =
        ASTDataDefinition(name, typ, constructors, derivedClasses)

    fun constant(value: Any): ASTExpression   = ASTConstant(value)
    fun variable(name: Symbol): ASTExpression = ASTVariable(name)
    fun variable(name: String): ASTExpression = ASTVariable(Symbol(name))

    fun apply(func: ASTExpression, vararg args: ASTExpression): ASTExpression =
        apply2(func, args)

    private fun apply2(func: ASTExpression, args: Array<out ASTExpression>): ASTExpression {
        var exp = func

        for (arg in args)
            exp = ASTApplication(exp, arg)

        return exp.simplify()
    }

    fun error(message: String): ASTExpression =
        AST.apply(AST.variable("error"), AST.constant(message))

    fun constructor(name: String, vararg args: ASTExpression): ASTExpression =
        apply2(ASTConstructor(name), args)

    fun lambda(argument: Symbol, body: ASTExpression): ASTExpression =
        ASTLambda(argument, body)

    fun lambda(arguments: List<Symbol>, body: ASTExpression): ASTExpression {
        if (arguments.isEmpty()) throw IllegalArgumentException("no arguments for lambda")

        val head = arguments.first()
        if (arguments.size == 1)
            return lambda(head, body)
        else
            return lambda(head, lambda(arguments.drop(1), body))
    }

    fun ifExp(test: ASTExpression, cons: ASTExpression, alt: ASTExpression): ASTExpression =
        caseExp(test, alternative(Pattern.Constructor(ConstructorNames.TRUE), cons),
                      alternative(Pattern.Constructor(ConstructorNames.FALSE), alt))

    fun caseExp(exp: ASTExpression, alts: List<ASTAlternative>): ASTExpression =
        ASTCase(exp, alts)

    fun caseExp(exp: ASTExpression, vararg alts: ASTAlternative): ASTExpression =
        ASTCase(exp, alts.toList())

    fun alternative(pattern: Pattern, exp: ASTExpression): ASTAlternative =
        ASTAlternative(pattern, exp);

    fun letRec(name: Symbol, value: ASTExpression, body: ASTExpression): ASTExpression =
        ASTLetRec(singletonList(ImplicitBinding(name, value)), body)

    fun let(recursive: Boolean, binding: ImplicitBinding, body: ASTExpression): ASTExpression {
        val bindings = singletonList(binding)
        return if (recursive) ASTLetRec(bindings, body) else ASTLet(bindings, body)
    }

    fun sequence(vararg exps: ASTExpression): ASTSequence =
        ASTSequence(exps.toList())

    fun tuple(exps: List<ASTExpression>): ASTExpression  {
        if (exps.isEmpty())
            return AST.constructor(ConstructorNames.UNIT)
        if (exps.size == 1)
            return exps.first()

        val name = ConstructorNames.tupleName(exps.size)

        var call = constructor(name)

        for (exp in exps)
            call = ASTApplication(call, exp)

        return call
    }

    fun set(name: Symbol, exp: ASTExpression): ASTExpression =
        ASTSet(name, exp)

    fun define(name: Symbol, value: ASTExpression): ASTValueDefinition =
        ASTValueDefinition(name, value)

    fun list(exps: List<ASTExpression>): ASTExpression {
        var list = constructor(ConstructorNames.NIL)

        for (exp in exps.reversed())
            list = constructor(ConstructorNames.CONS, exp, list)

        return list
    }
}
