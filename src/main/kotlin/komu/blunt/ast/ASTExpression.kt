package komu.blunt.ast

import komu.blunt.objects.Symbol
import komu.blunt.types.typeFromObject

sealed class ASTExpression {
    abstract override fun toString(): String
    open fun simplify(): ASTExpression = this

    class Application(val func: ASTExpression, val arg: ASTExpression) : ASTExpression() {

        fun map(f: (ASTExpression) -> ASTExpression) = Application(f(func), f(arg))

        override fun toString() = "($func $arg)"

        override fun simplify(): ASTExpression {
            val simplifiedFunc = func.simplify()
            val simplifiedArg = arg.simplify()

            if (simplifiedFunc is Lambda) {
                val bindings = listOf(ImplicitBinding(simplifiedFunc.argument, simplifiedArg))
                return Let(bindings, simplifiedFunc.body).simplify()
            } else
                return Application(simplifiedFunc, simplifiedArg)
        }
    }

    class Case(val exp: ASTExpression, val alternatives: List<ASTAlternative>) : ASTExpression() {
        override fun toString() = "case $exp of $alternatives"
        override fun simplify() = Case(exp.simplify(), alternatives.map { it.simplify() })
    }

    class Constant(val value: Any) : ASTExpression() {
        fun valueType() = typeFromObject(value)
        override fun toString() = value.toString()
        override fun simplify() = this
    }

    class Constructor(val name : String) : ASTExpression() {
        override fun toString() = name
        override fun simplify() = this
    }

    class Lambda(val argument: Symbol, val body: ASTExpression) : ASTExpression() {
        override fun toString() = "(\\ $argument -> $body)"
        override fun simplify() = Lambda(argument, body.simplify())
    }

    class Let(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {
        override fun toString() = "(let (${bindings.joinToString(" ")}) $body)"
        override fun simplify() = Let(bindings.map { it.simplify() }, body.simplify())
    }

    class LetRec(val bindings: List<ImplicitBinding>, val body: ASTExpression) : ASTExpression() {

        // TODO: convert letrecs to lets if variable is not referenced in binding
        override fun simplify() =
                LetRec(bindings.map { it.simplify() }, body.simplify())

        override fun toString() =
                "(letrec (${bindings.joinToString(" ")}) $body)"
    }

    class Sequence(val exps: List<ASTExpression>) : ASTExpression() {

        fun map(f: (ASTExpression) -> ASTExpression) = Sequence(exps.map(f))

        override fun toString() = exps.joinToString(" ", "(begin ", ")")

        override fun simplify(): ASTExpression {
            val simplified = exps.map { it.simplify() }
            return simplified.singleOrNull() ?: Sequence(simplified)
        }
    }

    class Set(val variable: Symbol, val exp: ASTExpression) : ASTExpression() {
        override fun toString() = "(set! $variable $exp)"
        override fun simplify() = Set(variable, exp.simplify())
    }

    class Variable(val name: Symbol) : ASTExpression() {
        override fun toString() = name.toString()
        override fun simplify() = this
    }
}

