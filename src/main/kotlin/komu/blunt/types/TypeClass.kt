package komu.blunt.types

import kotlin.util.*

class TypeClass(val superClasses: List<String>) {

    val instances = arrayList<ClassInstance>()

    fun addInstance(qual: Qualified<Predicate>) {
        instances.add(ClassInstance(qual))
    }

    fun instancePredicates(): Collection<Predicate> =
        instances.map { it.qual.value }
}
