package komu.blunt.parser

class Token<out T>(val tokenType: TokenType<T>, val value: T, val location: SourceLocation) {

    class object {
        fun ofType(t: TokenType<Unit>, location: SourceLocation): Token<Unit> = Token(t, #(), location)
    }

    fun toString() = tokenType.toString()

    fun asType<U>(t: TokenType<U>): Token<U> {
        if (this is Token<U>)
            return this
        else
            throw IllegalArgumentException("can't cast $this to $t");
    }
}
