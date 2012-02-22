package komu.blunt.ast

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorDefinition
import komu.blunt.types.ConstructorNames
import komu.blunt.types.Type
import komu.blunt.types.patterns.Pattern

import java.util.List

import com.google.common.collect.Lists.newArrayList
import java.util.Arrays
import java.util.ArrayList

/**
 * Convenience functions for constructing syntax objects.
 */
object AST {

    fun data(name: String, typ: Type, constructors: ImmutableList<ConstructorDefinition>, derivedClasses: ImmutableList<String>) =
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

        val head = arguments.get(0).sure()
        val tail = arguments.subList(1, arguments.size()).sure()
        if (arguments.size() == 1)
            return lambda(head, body)
        else
            return lambda(head, lambda(tail, body))
    }

    fun ifExp(test: ASTExpression, cons: ASTExpression, alt: ASTExpression): ASTExpression =
        caseExp(test, alternative(Pattern.constructor(ConstructorNames.TRUE.sure()), cons),
                      alternative(Pattern.constructor(ConstructorNames.FALSE.sure()), alt))

    fun caseExp(exp: ASTExpression, alts: ImmutableList<ASTAlternative>): ASTExpression =
        ASTCase(exp, alts)

    fun caseExp(exp: ASTExpression, vararg alts: ASTAlternative): ASTExpression {
        val lst = ArrayList<ASTAlternative>
        for (val alt in alts) lst.add(alt)

        return ASTCase(exp, ImmutableList.copyOf(lst).sure())
    }

    fun alternative(pattern: Pattern, exp: ASTExpression): ASTAlternative =
        ASTAlternative(pattern, exp);

    fun letRec(name: Symbol, value: ASTExpression, body: ASTExpression): ASTExpression =
        ASTLetRec(ImmutableList.of<ImplicitBinding?>(ImplicitBinding(name, value)).sure(), body)

    fun let(recursive: Boolean, binding: ImplicitBinding?, body: ASTExpression): ASTExpression {
        val bindings = ImmutableList.of<ImplicitBinding?>(binding).sure()
        return if (recursive) ASTLetRec(bindings, body) else ASTLet(bindings, body)
    }

    fun sequence(vararg exps: ASTExpression?): ASTSequence {
        val lst = ArrayList<ASTExpression?>
        for (val exp in exps) lst.add(exp)

        return ASTSequence(ImmutableList.copyOf(lst).sure())
    }

    fun tuple(exps: List<ASTExpression>): ASTExpression  {
        if (exps.isEmpty())
            return AST.constructor(ConstructorNames.UNIT.sure())
        if (exps.size() == 1)
            return exps.get(0)

        var call = constructor(ConstructorNames.tupleName(exps.size()).sure())

        for (val exp in exps)
            call = ASTApplication(call, exp)

        return call
    }

    fun set(name: Symbol, exp: ASTExpression): ASTExpression =
        ASTSet(name, exp)

    fun define(name: Symbol?, value: ASTExpression?): ASTValueDefinition =
        ASTValueDefinition(name.sure(), value.sure())

    fun listBuilder() = ListBuilder()

    class ListBuilder {
        private val exps = ArrayList<ASTExpression?>()

        fun add(exp: ASTExpression?) {
            exps.add(exp)
        }

        fun build(): ASTExpression {
            var list = constructor(ConstructorNames.NIL.sure())

            for (val exp in Lists.reverse(exps))
                list = constructor(ConstructorNames.CONS.sure(), exp.sure(), list)

            return list
        }
    }

    fun sequenceBuilder() = SequenceBuilder()

    class SequenceBuilder {
        private val exps = ArrayList<ASTExpression?>()

        fun add(exp: ASTExpression?) {
            exps.add(exp)
        }

        fun build(): ASTSequence =
            ASTSequence(ImmutableList.copyOf(exps).sure())
    }
}
