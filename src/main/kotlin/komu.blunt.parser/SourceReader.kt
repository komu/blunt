package komu.blunt.parser

class SourceReader(private val source: String) {

    private var line = 1
    private var column = 1
    private var position = 0

    fun read(): Char? {
        if (!hasMore()) return null

        val ch = source[position++]
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }

        return ch
    }

    fun matches(s: String): Boolean {
        if (s == "") throw IllegalArgumentException("empty string")

        if (!hasMore()) return false;

        //return source.regionMatches(position, s, 0, s.length())
        return source.substring(position, position+s.length()) == s
    }

    fun getLine() = line
    fun getColumn() = column

    fun peek(): Char? =
        if (hasMore())
            source[position]
        else
            null

    fun hasMore(): Boolean =
        position < source.length()


    fun getLocation(): SourceLocation =
        SourceLocation(line, column)

    fun getPosition() = position

    fun setPosition(position: Int) {
        if (position < 0 || position >= source.length)
            throw IllegalArgumentException("invalid position: $position")

        this.position = position;
    }
}

