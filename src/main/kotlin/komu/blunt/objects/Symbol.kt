package komu.blunt.objects

class Symbol (private val name: String) {
    init {
        require(!name.isEmpty()) { "empty name" }
    }
    override fun toString() = name
    override fun equals(other: Any?) = other is Symbol && name == other.name
    override fun hashCode() = name.hashCode()
}
