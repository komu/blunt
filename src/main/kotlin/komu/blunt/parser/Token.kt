package komu.blunt.parser

class Token<out T>(val tokenType: TokenType<T>, val value: T, val location: SourceLocation) {

    companion object {
        fun ofType(t: TokenType<Unit>, location: SourceLocation): Token<Unit> = Token(t, Unit, location)
    }

    override fun toString() = tokenType.toString()

    @Suppress("CAST_NEVER_SUCCEEDS")
    fun <U> asType(t: TokenType<U>): Token<U> =
        if (tokenType == t)
            this as Token<U>
        else
            throw Exception("can't cast token of type $tokenType to $t")
}
