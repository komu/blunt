package komu.blunt.types

import std.util.*
import komu.blunt.types.checker.Substitution

import java.util.Iterator
import java.util.List
import java.util.Set
import java.util.Objects.hash

class TypeConstructor(private val name: String, private val kind: Kind) : Type() {

    class object {
        private val tuplePattern = java.util.regex.Pattern.compile("\\(,+\\)").sure()
    }

    override fun apply(substitution: Substitution?) = this
    override fun hnf() = false
    override fun instantiate(vars: List<TypeVariable?>?) = this
    override fun addTypeVariables(variables: Set<TypeVariable?>?) { }
    override fun getKind() = kind
    override fun toString(precedence: Int) = name

    fun equals(rhs: Any?) =
        rhs is TypeConstructor && name == rhs.name && kind == rhs.kind

    fun hashCode() =
        hash(name, kind)

    internal fun toString(arguments: List<Type?>, precedence: Int): String =
        if (name == "->" && arguments.size == 2)
            functionToString(arguments, precedence);
        else if (name.equals("[]") && arguments.size == 1)
            "[" + arguments.first() + "]"
        else if (tuplePattern.matcher(name).sure().matches())
            tupleToString(arguments)
        else
            defaultToString(arguments, precedence)

    private fun defaultToString(arguments: List<Type?>, precedence: Int): String {
        val sb = StringBuilder()

        if (precedence != 0) sb.append("(")
        sb.append(name)

        for (val arg in arguments)
            sb.append(' ')?.append(arg?.toString(1))

        if (precedence != 0) sb.append(")")

        return sb.toString().sure()
    }

    private fun tupleToString(arguments: List<Type?>): String {
        val sb = StringBuilder()
        sb.append("(")

        val it = arguments.iterator().sure()
        while (it.hasNext()) {
            sb.append(it.next()?.toString(0))
            if (it.hasNext())
                sb.append(", ")
        }
        sb.append(")")
        return sb.toString().sure()
    }

    private fun functionToString(arguments: List<Type?>, precedence: Int): String {
        val sb = StringBuilder()

        if (precedence != 0) sb.append("(")
        sb.append(arguments.get(0)?.toString(1))
        sb.append(" -> ")
        sb.append(arguments.get(1)?.toString(0))
        if (precedence != 0) sb.append(")")

        return sb.toString().sure()
    }
}

