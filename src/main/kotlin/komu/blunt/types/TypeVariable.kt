package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.List
import java.util.Set
import java.util.Objects.hash

class TypeVariableImpl(private val name: String, private val kind: Kind) : TypeVariable() {

    override fun hnf() = true
    override fun toString(precedence: Int) = name
    override fun getKind() = kind
    override fun instantiate(vars: List<TypeVariable?>?) = this

    override fun apply(substitution: Substitution?): Type =
        substitution?.lookup(this) ?: this

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        variables?.add(this)
    }

    override fun equals(obj: Any?) =
        obj is TypeVariableImpl && name == obj.name && kind == obj.kind

    override fun hashCode() =
         hash(name, kind)
}
