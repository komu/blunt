package komu.blunt.types

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.Substitution

import java.util.LinkedList
import java.util.List
import java.util.Set
import java.util.Objects.hash

class TypeApplication(val left: Type, val right: Type) : Type() {

    override fun apply(substitution: Substitution) =
        TypeApplication(left.apply(substitution).sure(), right.apply(substitution).sure())

    override fun instantiate(vars: List<TypeVariable>) =
        TypeApplication(left.instantiate(vars), right.instantiate(vars))

    override fun hnf() = left.hnf()

    override fun addTypeVariables(result: Set<TypeVariable>) {
        left.addTypeVariables(result)
        right.addTypeVariables(result)
    }

    override val kind: Kind
        get() {
            val kind = left.kind
            if (kind is ArrowKind)
                return kind.right
            else
                throw TypeCheckException("invalid kind: $left")
        }

    override fun toString(precedence: Int): String =
        toString(LinkedList<Type?>(), precedence)

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
