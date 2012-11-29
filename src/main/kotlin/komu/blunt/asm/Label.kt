package komu.blunt.asm

class Label(private val name: String) {

    fun relocateBy(offset: Int) {
        require(offset >= 0, "negative offset: $offset")

        address += offset
    }

    var address: Int = -1
        get() {
            check($address != -1, "address not initialized")
            return $address
        }

        set(value) {
            require(value >= 0, "negative address: $value")
            $address = value
        }

    fun toString() = name
}
