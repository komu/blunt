package komu.blunt.types

import komu.blunt.types.checker.Substitution

class ClassInstance(val qual: Qualified<Predicate>) : Types<ClassInstance> {

    val predicates: List<Predicate>
        get() = qual.predicates

    val predicate: Predicate
        get() = qual.value

    override fun addTypeVariables(result: MutableSet<Type.Var>) {
        qual.addTypeVariables(result)
    }

    override fun apply(substitution: Substitution): ClassInstance =
        ClassInstance(qual.apply(substitution))

    override fun toString() =
        qual.toString()
}
