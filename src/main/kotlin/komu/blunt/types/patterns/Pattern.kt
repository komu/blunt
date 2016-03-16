package komu.blunt.types.patterns

import komu.blunt.objects.Symbol
import komu.blunt.types.ConstructorNames

abstract class Pattern {
    override abstract fun toString(): String

    companion object {
        fun constructor(name: String, vararg args: Pattern): Pattern =
            ConstructorPattern(name, args.toList())

        fun constructor(name: String, args: List<Pattern>): Pattern =
            ConstructorPattern(name, args)

        fun variable(name: String): Pattern =
            variable(Symbol(name))

        fun variable(name: Symbol): Pattern =
            VariablePattern(name)

        fun literal(value: Any): Pattern =
            LiteralPattern(value)

        fun wildcard(): Pattern =
            WildcardPattern.INSTANCE

        fun tuple(args: List<Pattern>): Pattern =
            when (args.size) {
                0    -> constructor(ConstructorNames.UNIT)
                1    -> args.first()
                else -> constructor(ConstructorNames.tupleName(args.size), args)
            }
    }
}

class ConstructorPattern(val name: String, val args: List<Pattern>) : Pattern() {

    fun map(f: (Pattern) -> Pattern) = ConstructorPattern(name, args.map(f))

    override fun toString(): String =
        if (args.isEmpty())
            name
        else
            args.joinToString(" ", "($name", ")")

    override fun equals(obj: Any?) = obj is ConstructorPattern && name == obj.name && args == obj.args
    override fun hashCode() = name.hashCode() * 79 + args.hashCode()
}

class WildcardPattern private constructor() : Pattern() {

    companion object {
        val INSTANCE = WildcardPattern()
    }

    override fun toString() = "_"
}

class VariablePattern(val variable: Symbol) : Pattern() {
    override fun toString() = variable.toString()
    override fun equals(obj: Any?) = obj is VariablePattern && variable == obj.variable
    override fun hashCode() = variable.hashCode()
}

class LiteralPattern(val value: Any) : Pattern() {
    override fun toString() = value.toString()
    override fun equals(obj: Any?) = obj is LiteralPattern && value == obj.value
    override fun hashCode() = value.hashCode()
}
