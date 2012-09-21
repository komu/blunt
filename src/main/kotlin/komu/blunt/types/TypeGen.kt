package komu.blunt.types

import komu.blunt.types.checker.Substitution

class TypeGen(private val index: Int) : Type() {

    override fun apply(substitution: Substitution) = this
    override fun instantiate(vars: List<TypeVariable>) = vars[index]
    override fun addTypeVariables(result: MutableSet<TypeVariable>) { }
    override val kind: Kind
        get() = throw RuntimeException("can't access kind of TypeGen")
    override fun hnf(): Boolean = throw RuntimeException("should not call hnf for TypeGen")
    override fun toString(precedence: Int) = "TypeGen[$index]"
}

