package komu.blunt.types

import java.util.ArrayList

class TypeClass(val superClasses: List<String>) {

    val instances: MutableList<ClassInstance> = ArrayList<ClassInstance>()

    fun addInstance(qual: Qualified<Predicate>) {
        instances.add(ClassInstance(qual))
    }

    val instancePredicates: Collection<Predicate>
        get() = instances.map { it.qual.value }
}
