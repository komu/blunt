package komu.blunt.asm

class Label(private val name: String) {

    private var _address = -1

    protected fun relocateBy(offset: Int) {
        if (offset < 0) throw IllegalArgumentException("negative offset: $offset")
        if (_address == -1) throw IllegalStateException("can't relocate label without original address")

        _address += offset
    }

    var address: Int
        get() {
            if (_address == -1) throw IllegalStateException("address not initialized")

            return _address
        }
        set(value) {
            if (value < 0) throw IllegalArgumentException("negative address")

            if (this._address != -1)
                throw IllegalStateException("address already set")

            this._address = value
        }

    fun toString() = name
}
