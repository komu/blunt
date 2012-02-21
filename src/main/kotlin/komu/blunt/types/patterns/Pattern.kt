package komu.blunt.types.patterns

import komu.blunt.objects.Symbol
import com.google.common.base.Objects
import com.google.common.collect.ImmutableList

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

    override fun equals(obj: Any?) = obj is ConstructorPattern && name == obj.name && args == obj.args
    override fun hashCode(): Int = Objects.hashCode(name) * 79 + args.hashCode()
}

class WildcardPattern private () : Pattern() {

    class object {
        val INSTANCE = WildcardPattern()
    }

    override fun toString() = "_"
}

class VariablePattern(val variable: Symbol) : Pattern() {
    override fun toString() = variable.toString().sure()
    override fun equals(obj: Any?) = obj is VariablePattern && variable == obj.variable
    override fun hashCode(): Int = variable.hashCode()
}

class LiteralPattern(val value: Any?) : Pattern() {
    override fun toString() = value.toString().sure()
    override fun equals(obj: Any?) = obj is LiteralPattern && value == obj.value
    override fun hashCode(): Int = Objects.hashCode(value)
}
