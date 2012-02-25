package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.List
import java.util.Set
import java.util.Objects.hash

class TypeVariable(private val name: String, private val _kind: Kind) : Type() {

    override fun hnf() = true
    override fun toString(precedence: Int) = name
    override val kind = _kind
    override fun instantiate(vars: List<TypeVariable>) = this

    override fun apply(substitution: Substitution): Type =
        substitution.lookup(this) ?: this

    override fun addTypeVariables(variables: Set<TypeVariable>) {
        variables.add(this)
    }

    fun equals(obj: Any?) =
        obj is TypeVariable && name == obj.name && kind == obj.kind

    fun hashCode() =
        hash(name, kind)
}
