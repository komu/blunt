package komu.blunt.types

import komu.blunt.eval.TypeCheckException
import komu.blunt.types.checker.Substitution
import java.util.*
import java.util.Objects.hash

class TypeApplication(val left: Type, val right: Type) : Type() {

    override fun apply(substitution: Substitution) =
        TypeApplication(left.apply(substitution), right.apply(substitution))

    override fun instantiate(vars: List<TypeVariable>) =
        TypeApplication(left.instantiate(vars), right.instantiate(vars))

    override val hnf: Boolean
        get() = left.hnf

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
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
            is TypeApplication -> left.toString(arguments, precedence)
            is TypeConstructor -> left.toString(arguments, precedence)
            else               -> return "($left $arguments)"
        }
    }

    override fun equals(other: Any?) = other is TypeApplication && left == other.left && right == other.right
    override fun hashCode() =  hash(left, right)
}
