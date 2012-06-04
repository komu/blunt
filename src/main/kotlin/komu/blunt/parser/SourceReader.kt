package komu.blunt.parser

class SourceReader(private val source: String) {

    private var _line = 1
    private var _column = 1
    private var _position = 0

    fun read(): Char? {
        if (!hasMore()) return null

        val ch = source[_position++]
        if (ch == '\n') {
            _line++
            _column = 1
        } else {
            _column++
        }

        return ch
    }

    fun matches(s: String): Boolean {
        if (s == "") throw IllegalArgumentException("empty string")

        if (!hasMore()) return false;

        return source.regionMatches(position, s, 0, s.length())
    }

    fun peek(): Char? =
        if (hasMore())
            source[_position]
        else
            null

    fun hasMore(): Boolean =
        _position < source.length()

    val line: Int
        get() = _line

    val column: Int
        get() = _column

    val location: SourceLocation
        get() = SourceLocation(_line, _column)

    var position: Int
        get() = _position
        set(position) {
            if (position < 0 || position >= source.length)
                throw IllegalArgumentException("invalid position: $position")

            this._position = position
        }
}
