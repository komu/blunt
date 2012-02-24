package komu.blunt.types

import komu.blunt.types.checker.Substitution

import java.util.List
import java.util.Set

class ClassInstance(val qual: Qualified<Predicate>) : Types<ClassInstance> {

    val predicates: List<Predicate>
        get() = qual.predicates

    val predicate: Predicate
        get() = qual.value

    override fun addTypeVariables(variables: Set<TypeVariable?>?) {
        qual.addTypeVariables(variables)
    }

    override fun apply(substitution: Substitution?): ClassInstance =
        ClassInstance(qual.apply(substitution).sure())

    fun toString() =
        qual.toString()
}
