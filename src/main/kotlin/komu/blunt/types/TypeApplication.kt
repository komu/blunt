package komu.blunt.types

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.Substitution

import java.util.LinkedList
import java.util.List
import java.util.Set
import java.util.Objects.hash

class TypeApplication(val left: Type, val right: Type) : Type() {

    override fun apply(substitution: Substitution?) =
        TypeApplication(left.apply(substitution).sure(), right.apply(substitution).sure())

    override fun instantiate(vars: List<TypeVariable?>?) =
        TypeApplication(left.instantiate(vars).sure(), right.instantiate(vars).sure());

    override fun hnf() = left.hnf()

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        left.addTypeVariables(variables)
        right.addTypeVariables(variables)
    }

    override fun getKind(): Kind {
        val kind = left.getKind()
        if (kind is ArrowKind)
            return kind.right.sure()
        else
            throw TypeCheckException("invalid kind: $left")
    }

    override fun toString(precedence: Int): String =
        toString(LinkedList<Type?>, precedence)

    private fun toString(arguments: LinkedList<Type?>, precedence: Int): String {
        arguments.addFirst(right)
        return when (left) {
            is TypeApplication -> left.toString(arguments, precedence)
            is TypeConstructor -> left.toString(arguments, precedence).sure()
            else               -> return "($left $arguments)"
        }
    }

    fun equals(rhs: Any?) =
        rhs is TypeApplication && left == rhs.left && right == rhs.right

    fun hashCode() =
        hash(left, right)
}
