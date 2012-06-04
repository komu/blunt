package komu.blunt.types

import kotlin.util.*
import java.util.Collection
import komu.blunt.types.checker.UnificationException
import komu.blunt.types.checker.Unifier

import komu.blunt.types.checker.Substitution;

import java.util.List;
import java.util.Set;
import java.util.Objects.hash

fun isIn(className: String, typ: Type) = Predicate(className, typ)

class Predicate(val className: String, val `type`: Type) : Types<Predicate?> {

    fun inHnf() = `type`.hnf()

    override fun addTypeVariables(variables: Set<TypeVariable>) {
        `type`.addTypeVariables(variables)
    }

    override fun apply(substitution: Substitution): Predicate =
        Predicate(className, `type`.apply(substitution).sure())

    fun instantiate(ts: List<TypeVariable>) =
        Predicate(className, `type`.instantiate(ts))

    fun overlapsAny(predicates: Collection<Predicate>): Boolean =
        predicates.any { overlaps(it) }

    fun overlaps(predicate: Predicate): Boolean {
        try {
            Unifier.mguPredicate(this, predicate)
            return true
        } catch (e: UnificationException) {
            return false
        }
    }

    fun equals(rhs: Any?) =
        rhs is Predicate && className == rhs.className && `type` == rhs.`type`

    fun hashCode() =
        hash(className, `type`)

    fun toString() =
        "$className ${`type`}"
}
