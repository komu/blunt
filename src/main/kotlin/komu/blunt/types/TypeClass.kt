package komu.blunt.types

import java.util.ArrayList
import java.util.Collection
import java.util.List
import kotlin.util.*

class TypeClass(val superClasses: List<String>) {

    val instances = ArrayList<ClassInstance>()

    fun addInstance(qual: Qualified<Predicate>) {
        instances.add(ClassInstance(qual))
    }

    fun instancePredicates(): Collection<Predicate> =
        instances.map { it.qual.value }
}
