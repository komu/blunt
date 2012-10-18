package komu.blunt.types

class TypeClass(val superClasses: List<String>) {

    val instances: MutableList<ClassInstance> = arrayList<ClassInstance>()

    fun addInstance(qual: Qualified<Predicate>) {
        instances.add(ClassInstance(qual))
    }

    val instancePredicates: Collection<Predicate>
        get() = instances.map { it.qual.value }
}
