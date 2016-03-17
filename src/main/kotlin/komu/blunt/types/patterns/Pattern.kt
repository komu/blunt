package komu.blunt.types.patterns

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorNames

sealed class Pattern {
    override abstract fun toString(): String

    companion object {


        fun variable(name: Symbol): Pattern = Variable(name)

        fun tuple(args: List<Pattern>): Pattern =
            when (args.size) {
                0    -> Constructor(ConstructorNames.UNIT)
                1    -> args.first()
                else -> Constructor(ConstructorNames.tupleName(args.size), args)
            }
    }

    class Constructor(val name: String, val args: List<Pattern>) : Pattern() {

        constructor(name: String, vararg args: Pattern): this(name, args.asList()) { }

        fun map(f: (Pattern) -> Pattern) = Constructor(name, args.map(f))

        override fun toString(): String =
                if (args.isEmpty())
                    name
                else
                    args.joinToString(" ", "($name", ")")

        override fun equals(other: Any?) = other is Constructor && name == other.name && args == other.args
        override fun hashCode() = name.hashCode() * 79 + args.hashCode()
    }

    object Wildcard : Pattern() {
        override fun toString() = "_"
    }

    class Variable(val variable: Symbol) : Pattern() {
        override fun toString() = variable.toString()
        override fun equals(other: Any?) = other is Variable && variable == other.variable
        override fun hashCode() = variable.hashCode()
    }

    class Literal(val value: Any) : Pattern() {
        override fun toString() = value.toString()
        override fun equals(other: Any?) = other is Literal && value == other.value
        override fun hashCode() = value.hashCode()
    }
}
