package komu.blunt.types

import komu.blunt.types.checker.Substitution
import komu.blunt.types.checker.UnificationException
import komu.blunt.types.checker.Unifier

fun isIn(className: String, typ: Type) = Predicate(className, typ)

data class Predicate(val className: String, val `type`: Type) : Types<Predicate> {

    fun inHnf() = `type`.hnf()

    override fun addTypeVariables(result: MutableSet<TypeVariable>) {
        `type`.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): Predicate =
        Predicate(className, `type`.apply(substitution))

    fun instantiate(ts: List<TypeVariable>) =
        Predicate(className, `type`.instantiate(ts))

    fun overlapsAny(predicates: Collection<Predicate>) =
        predicates.any { overlaps(it) }

    fun overlaps(predicate: Predicate): Boolean =
        try {
            Unifier.mguPredicate(this, predicate)
            true
        } catch (e: UnificationException) {
            false
        }
}
