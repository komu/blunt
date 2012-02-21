package komu.blunt.types.patterns

import komu.blunt.objects.Symbol
import com.google.common.base.Objects
import com.google.common.collect.ImmutableList
import komu.blunt.types.ConstructorNames;

abstract class Pattern {

    abstract fun toString(): String

    class object {
        fun constructor(name: String, vararg args: Pattern?) =
            throw UnsupportedOperationException() //return new ConstructorPattern(name, ImmutableList.copyOf(args));

        fun constructor(name: String, args: ImmutableList<Pattern?>) =
            ConstructorPattern(name, args)

        fun variable(name: String) = variable(Symbol.symbol(name).sure())

        fun variable(name: Symbol) = VariablePattern(name)

        fun literal(value: Any?) = LiteralPattern(value)

        fun wildcard() = WildcardPattern.INSTANCE

        fun tuple(args: ImmutableList<Pattern?>): Pattern  {
            if (args.isEmpty())
                return constructor(ConstructorNames.UNIT.sure())
            if (args.size() == 1)
                return args.get(0).sure()
            else
                return constructor(ConstructorNames.tupleName(args.size()), args);
        }
    }
}


class ConstructorPattern(val name: String, val args: ImmutableList<Pattern?>) : Pattern() {

    override fun toString(): String {
        if (args.isEmpty())
            return name

        val sb = StringBuilder()
        sb.append('(')
        sb.append(name)

        for (val arg in args)
            sb.append(' ')?.append(arg)
        sb.append(')')

        return sb.toString().sure()
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
    override fun toString() = variable.toString().sure()
    fun equals(obj: Any?) = obj is VariablePattern && variable == obj.variable
    fun hashCode(): Int = variable.hashCode()
}

class LiteralPattern(val value: Any?) : Pattern() {
    override fun toString() = value.toString().sure()
    fun equals(obj: Any?) = obj is LiteralPattern && value == obj.value
    fun hashCode(): Int = Objects.hashCode(value)
}
