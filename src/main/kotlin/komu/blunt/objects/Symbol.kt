package komu.blunt.objects

class Symbol (private val value: String) {
    fun toString() = value
    fun equals(obj: Any?) = obj is Symbol && value == obj.value
    fun hashCode() = java.util.Objects.hash(value)
}
