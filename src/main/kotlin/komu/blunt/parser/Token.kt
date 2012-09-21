package komu.blunt.parser

class Token<out T>(val tokenType: TokenType<T>, val value: T, val location: SourceLocation) {

    class object {
        fun ofType(t: TokenType<Unit>, location: SourceLocation): Token<Unit> = Token(t, Unit.VALUE, location)
    }

    fun toString() = tokenType.toString()

    fun asType<U>(t: TokenType<U>): Token<U> =
        this as Token<U>
}
