package komu.blunt.parser

class Token<out T>(val `type`: TokenType<T>, val value: T?, val location: SourceLocation) {

    this(t: TokenType<T>, location: SourceLocation): this(t, null, location) { }

    class object {
        fun ofType<T>(t: TokenType<T>, location: SourceLocation?): Token<T> = Token(t, location.sure())
    }

    fun toString() = `type`.toString()

    fun asType<U>(t: TokenType<U>): Token<U> {
        if (this is Token<U>)
            return this
        else
            throw IllegalArgumentException("can't cast $this to $t");
    }
}
