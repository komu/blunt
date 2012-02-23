package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.List
import java.util.Set

class TypeGen(private val index: Int) : Type() {

    override fun apply(s: Substitution?) = this
    override fun instantiate(vars: List<TypeVariable?>?) = vars?.get(index)
    override fun addTypeVariables(result: Set<TypeVariable?>?) { }
    override fun getKind() = throw RuntimeException("can't access kind of TypeGen")
    override fun hnf() = throw RuntimeException("should not call hnf for TypeGen")
    override fun toString(precedence: Int) = "TypeGen[$index]"
}
