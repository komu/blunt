package komu.blunt.objects

class Symbol (private val name: String) {
    init {
        require(!name.isEmpty()) { "empty name" }
    }
    override fun toString() = name
    override fun equals(obj: Any?) = obj is Symbol && name == obj.name
    override fun hashCode() = name.hashCode()
}
