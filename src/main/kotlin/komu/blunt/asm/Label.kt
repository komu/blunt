package komu.blunt.asm

class Label(private val name: String) {

    fun relocateBy(offset: Int) {
        require(offset >= 0) { "negative offset: $offset" }

        address += offset
    }

    private var _address = -1;

    var address: Int
        get() {
            check(_address != -1) { "address not initialized" }
            return _address
        }

        set(value) {
            require(value >= 0) { "negative address: $value" }
            _address = value
        }

    override fun toString() = name
}
