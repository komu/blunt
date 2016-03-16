package komu.blunt.types

import java.util.Objects.hash
import komu.blunt.types.checker.Substitution

class TypeVariable(private val name: String, private val _kind: Kind) : Type() {

    override val hnf = true
    override fun toString(precedence: Int) = name
    override val kind = _kind
    override fun instantiate(vars: List<TypeVariable>) = this

    override fun apply(substitution: Substitution): Type =
        substitution.lookup(this) ?: this

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        result.add(this)
    }

    override fun equals(obj: Any?) =
        obj is TypeVariable && name == obj.name && kind == obj.kind

    override fun hashCode() =
        hash(name, kind)
}
