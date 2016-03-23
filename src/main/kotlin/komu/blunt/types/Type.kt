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

        override fun addTypeVariables(result: MutableSet<Var>) {
            left.addTypeVariables(result)
            right.addTypeVariables(result)
        }

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
                else               -> return "($left $arguments)"
            }
        }

        override fun equals(other: Any?) = other is App && left == other.left && right == other.right
        override fun hashCode() = Objects.hash(left, right)
    }

    class Con(private val name: String, private val _kind: Kind) : Type() {

        companion object {
            private val tuplePattern = Regex("\\(,+\\)")
        }

        override fun apply(substitution: Substitution) = this
        override val hnf = false
        override fun instantiate(vars: List<Var>) = this
        override fun addTypeVariables(result: MutableSet<Var>) { }
        override val kind = _kind
        override fun toString(precedence: Int) = name

        override fun equals(other: Any?) = other is Con && name == other.name && kind == other.kind
        override fun hashCode() = Objects.hash(name, kind)

        internal fun toString(arguments: List<Type>, precedence: Int): String =
                when {
                    name == "->" && arguments.size == 2 ->
                        functionToString(arguments, precedence);
                    name.equals("[]") && arguments.size == 1 ->
                        "[" + arguments.first() + "]"
                    name.matches(tuplePattern) ->
                        tupleToString(arguments)
                    else ->
                        defaultToString(arguments, precedence)
                }

        private fun defaultToString(arguments: List<Type>, precedence: Int): String {
            val sb = StringBuilder()

            if (precedence != 0) sb.append("(")
            sb.append(name)

            for (arg in arguments)
                sb.append(' ').append(arg.toString(1))

            if (precedence != 0) sb.append(")")

            return sb.toString()
        }

        private fun tupleToString(arguments: List<Type>) =
                arguments.joinToString(", ", "(", ")")

        private fun functionToString(arguments: List<Type>, precedence: Int): String {
            val sb = StringBuilder()

            if (precedence != 0) sb.append("(")
            sb.append(arguments[0].toString(1))
            sb.append(" -> ")
            sb.append(arguments[1].toString(0))
            if (precedence != 0) sb.append(")")

            return sb.toString()
        }
    }

    class Gen(private val index: Int) : Type() {

        override fun apply(substitution: Substitution) = this
        override fun instantiate(vars: List<Var>) = vars[index]
        override fun addTypeVariables(result: MutableSet<Var>) { }
        override val kind: Kind
            get() = throw RuntimeException("can't access kind of TypeGen")
        override val hnf: Boolean
            get() = throw RuntimeException("should not call hnf for TypeGen")
        override fun toString(precedence: Int) = "TypeGen[$index]"
    }

    class Var(private val name: String, private val _kind: Kind) : Type() {

        override val hnf = true
        override fun toString(precedence: Int) = name
        override val kind = _kind
        override fun instantiate(vars: List<Var>) = this

        override fun apply(substitution: Substitution): Type = substitution.lookup(this) ?: this

        override fun addTypeVariables(result: MutableSet<Var>) {
            result.add(this)
        }

        override fun equals(other: Any?) = other is Var && name == other.name && kind == other.kind
        override fun hashCode() = Objects.hash(name, kind)
    }
}

fun List<Type>.kinds() = map { it.kind }

