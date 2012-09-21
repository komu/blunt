package komu.blunt.types.patterns

import komu.blunt.objects.Symbol
import com.google.common.base.Objects
import com.google.common.collect.ImmutableList
import komu.blunt.types.ConstructorNames;

abstract class Pattern {
    abstract fun toString(): String

    class object {
        fun constructor(name: String, vararg args: Pattern): Pattern =
            ConstructorPattern(name, ImmutableList.copyOf(args.toList()))

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

        fun tuple(args: List<Pattern>): Pattern  {
            if (args.isEmpty())
                return constructor(ConstructorNames.UNIT)
            if (args.size() == 1)
                return args.get(0)
            else
                return constructor(ConstructorNames.tupleName(args.size()), args);
        }
    }
}

class ConstructorPattern(val name: String, val args: List<Pattern>) : Pattern() {

    override fun toString(): String {
        if (args.isEmpty())
            return name

        val sb = StringBuilder()
        sb.append('(')
        sb.append(name)

        for (val arg in args)
            sb.append(' ').append(arg)
        sb.append(')')

        return sb.toString()
    }

    fun equals(obj: Any?) = obj is ConstructorPattern && name == obj.name && args == obj.args
    fun hashCode(): Int = Objects.hashCode(name) * 79 + args.hashCode()
}

class WildcardPattern private () : Pattern() {

    class object {
        val INSTANCE = WildcardPattern()
    }

    override fun toString() = "_"
}

class VariablePattern(val variable: Symbol) : Pattern() {
    override fun toString() = variable.toString()
    fun equals(obj: Any?) = obj is VariablePattern && variable == obj.variable
    fun hashCode(): Int = variable.hashCode()
}

class LiteralPattern(val value: Any) : Pattern() {
    override fun toString(): String = value.toString()
    fun equals(obj: Any?) = obj is LiteralPattern && value == obj.value
    fun hashCode(): Int = Objects.hashCode(value)
}
