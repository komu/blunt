package komu.blunt.types

import java.util.ArrayList
import java.util.Collection
import java.util.List
import std.util.*

class TypeClass(val superClasses: List<String?>) {

    val instances = ArrayList<ClassInstance?>()

    fun addInstance(qual: Qualified<Predicate>) {
        instances.add(ClassInstance(qual))
    }

    fun instancePredicates(): Collection<Predicate> {
        val result = ArrayList<Predicate>(instances.size)
        return instances.map(result) { it.sure().qual.value }
    }
}

