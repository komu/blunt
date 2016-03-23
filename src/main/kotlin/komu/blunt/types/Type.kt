package komu.blunt.types

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.Substitution
import java.util.*
import java.util.Collections.emptyList

sealed class Type : Types<Type> {
    abstract fun instantiate(vars: List<Var>): Type
    abstract val hnf: Boolean
    abstract val kind: Kind

    override fun toString() = toString(0)
    fun toScheme() = Scheme(emptyList(), Qualified.simple(this))

    protected abstract fun toString(precedence: Int): String

    class App(val left: Type, val right: Type) : Type() {

        override fun apply(substitution: Substitution) =
            App(left.apply(substitution), right.apply(substitution))

        override fun instantiate(vars: List<Var>) =
            App(left.instantiate(vars), right.instantiate(vars))

        override val hnf: Boolean
            get() = left.hnf

        override fun typeVars(): Sequence<Var> = left.typeVars() + right.typeVars()

        override val kind: Kind
            get() {
                val kind = left.kind
                if (kind is Kind.Arrow)
                    return kind.right
                else
                    throw TypeCheckException("invalid kind: $left")
            }

        override fun toString(precedence: Int): String =
            toString(LinkedList<Type>(), precedence)

        private fun toString(arguments: LinkedList<Type>, precedence: Int): String {
            arguments.addFirst(right)
            return when (left) {
                is App -> left.toString(arguments, precedence)
                is Con -> left.toString(arguments, precedence)
                else   -> return "($left $arguments)"
            }
        }

        override fun equals(other: Any?) = other is App && left == other.left && right == other.right
        override fun hashCode() = Objects.hash(left, right)
    }

    class Con(private val name: String, override val kind: Kind) : Type() {

        companion object {
            private val tuplePattern = Regex("\\(,+\\)")
        }

        override fun apply(substitution: Substitution) = this
        override val hnf = false
        override fun instantiate(vars: List<Var>) = this
        override fun typeVars(): Sequence<Var> = emptySequence()
        override fun toString(precedence: Int) = name
        override fun equals(other: Any?) = other is Con && name == other.name && kind == other.kind
        override fun hashCode() = Objects.hash(name, kind)

        internal fun toString(arguments: List<Type>, precedence: Int): String = when {
            name == "->" && arguments.size == 2 ->
                functionToString(arguments, precedence);
            name.equals("[]") && arguments.size == 1 ->
                "[${arguments.first()}]"
            name.matches(tuplePattern) ->
                tupleToString(arguments)
            else ->
                defaultToString(arguments, precedence)
        }

        private fun defaultToString(arguments: List<Type>, precedence: Int): String =
            arguments.joinToString(" ", name) { it.toString(1) }.parensIfNeeded(precedence)

        private fun tupleToString(arguments: List<Type>) =
            arguments.joinToString(", ", "(", ")")

        private fun functionToString(arguments: List<Type>, precedence: Int): String =
            "${arguments[0].toString(1)} -> ${arguments[1].toString(0)}".parensIfNeeded(precedence)

        private fun String.parensIfNeeded(precedence: Int): String =
            if (precedence != 0) "($this)" else this
    }

    class Gen(private val index: Int) : Type() {
        override fun apply(substitution: Substitution) = this
        override fun instantiate(vars: List<Var>) = vars[index]
        override fun typeVars(): Sequence<Var> = emptySequence()
        override val kind: Kind
            get() = throw RuntimeException("can't access kind of TypeGen")
        override val hnf: Boolean
            get() = throw RuntimeException("should not call hnf for TypeGen")
        override fun toString(precedence: Int) = "TypeGen[$index]"
    }

    class Var(private val name: String, override val kind: Kind = Kind.Star) : Type() {
        override val hnf = true
        override fun toString(precedence: Int) = name
        override fun instantiate(vars: List<Var>) = this
        override fun apply(substitution: Substitution): Type = substitution[this] ?: this
        override fun typeVars(): Sequence<Var> = sequenceOf(this)
        override fun equals(other: Any?) = other is Var && name == other.name && kind == other.kind
        override fun hashCode() = Objects.hash(name, kind)
    }

    companion object {
        fun list(t: Type) =
                Type.App(Type.Con("[]", Kind.ofParams(1)), t)

        fun function(argumentType: Type, returnType: Type) =
                generic("->", argumentType, returnType)

        fun function(args: List<Type>, resultType: Type): Type =
            if (args.isEmpty())
                resultType
            else
                function(args.first(), function(args.drop(1), resultType))

        fun tuple(vararg types: Type): Type =
                tuple(types.asList())

        fun tuple(types: List<Type>): Type =
                generic(ConstructorNames.tupleName(types.size), types)

        fun tupleOrSingle(types: List<Type>): Type =
                types.singleOrNull() ?: tuple(types)

        fun generic(name: String, vararg params: Type): Type =
                generic(name, params.asList())

        fun generic(name: String, params: List<Type>): Type =
                params.fold(Type.Con(name, Kind.ofParams(params.size)) as Type) { l, r -> Type.App(l, r) }

    }
}
