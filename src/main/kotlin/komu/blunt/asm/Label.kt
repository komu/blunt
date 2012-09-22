package komu.blunt.asm

class Label(private val name: String) {

    fun relocateBy(offset: Int) {
        check(offset >= 0, "negative offset: $offset")

        address += offset
    }

    var address: Int = -1
        get() {
            check($address != -1, "address not initialized")
            return $address
        }

        set(value) {
            check(value >= 0, "negative address: $value")
            check($address == -1, "address already set")

            $address = value
        }

    fun toString() = name
}
