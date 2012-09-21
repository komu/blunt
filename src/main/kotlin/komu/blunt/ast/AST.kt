package komu.blunt.ast

import java.util.Collections.singletonList
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

    fun constant(value: Any): ASTExpression   = ASTConstant(value)
    fun variable(name: Symbol): ASTExpression = ASTVariable(name)
    fun variable(name: String): ASTExpression = ASTVariable(Symbol(name))

    fun apply(func: ASTExpression, vararg args: ASTExpression): ASTExpression =
        apply2(func, args)

    private fun apply2(func: ASTExpression, args: Array<ASTExpression>): ASTExpression {
        var exp = func

        for (val arg in args)
            exp = ASTApplication(exp, arg)

        return exp.simplify()
    }

    fun constructor(name: String, vararg args: ASTExpression): ASTExpression =
        apply2(ASTConstructor(name), args)

    fun lambda(argument: Symbol, body: ASTExpression): ASTExpression =
        ASTLambda(argument, body)

    fun lambda(arguments: List<Symbol>, body: ASTExpression): ASTExpression {
        if (arguments.isEmpty()) throw IllegalArgumentException("no arguments for lambda")

        val head = arguments[0]
        val tail = arguments.subList(1, arguments.size())
        if (arguments.size() == 1)
            return lambda(head, body)
        else
            return lambda(head, lambda(tail, body))
    }

    fun ifExp(test: ASTExpression, cons: ASTExpression, alt: ASTExpression): ASTExpression =
        caseExp(test, alternative(Pattern.constructor(ConstructorNames.TRUE), cons),
                      alternative(Pattern.constructor(ConstructorNames.FALSE), alt))

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

        for (val exp in exps)
            call = ASTApplication(call, exp)

        return call
    }

    fun set(name: Symbol, exp: ASTExpression): ASTExpression =
        ASTSet(name, exp)

    fun define(name: Symbol, value: ASTExpression): ASTValueDefinition =
        ASTValueDefinition(name, value)

    fun bluntListBuilder() = ListBuilder()

    class ListBuilder {
        private val exps = arrayList<ASTExpression>()

        fun add(exp: ASTExpression) {
            exps.add(exp)
        }

        fun build(): ASTExpression {
            var list = constructor(ConstructorNames.NIL)

            for (val exp in exps.reverse())
                list = constructor(ConstructorNames.CONS, exp, list)

            return list
        }
    }

    fun sequenceBuilder() = SequenceBuilder()

    class SequenceBuilder {
        private val exps = listBuilder<ASTExpression>()

        fun add(exp: ASTExpression) {
            exps.add(exp)
        }

        fun build(): ASTSequence =
            ASTSequence(exps.build())
    }
}
