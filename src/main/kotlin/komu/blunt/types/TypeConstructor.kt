package komu.blunt.types

import java.util.Objects.hash
import komu.blunt.types.checker.Substitution

class TypeConstructor(private val name: String, private val _kind: Kind) : Type() {

    class object {
        private val tuplePattern = java.util.regex.Pattern.compile("\\(,+\\)")
    }

    override fun apply(substitution: Substitution) = this
    override val hnf = false
    override fun instantiate(vars: List<TypeVariable>) = this
    override fun addTypeVariables(result: MutableSet<TypeVariable>) { }
    override val kind = _kind
    override fun toString(precedence: Int) = name

    fun equals(rhs: Any?) =
        rhs is TypeConstructor && name == rhs.name && kind == rhs.kind

    fun hashCode() =
        hash(name, kind)

    internal fun toString(arguments: List<Type>, precedence: Int): String =
        when {
            name == "->" && arguments.size == 2 ->
                functionToString(arguments, precedence);
            name.equals("[]") && arguments.size == 1 ->
                "[" + arguments.first() + "]"
            tuplePattern.matcher(name).matches() ->
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
        arguments.makeString(", ", "(", ")")

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

