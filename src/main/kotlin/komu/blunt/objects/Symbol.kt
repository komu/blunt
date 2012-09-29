package komu.blunt.objects

class Symbol (private val name: String) {
    {
        require(!name.isEmpty(), "empty name")
    }
    fun toString() = name
    fun equals(obj: Any?) = obj is Symbol && name == obj.name
    fun hashCode() = name.hashCode()
}
