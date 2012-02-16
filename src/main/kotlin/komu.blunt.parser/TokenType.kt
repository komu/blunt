package komu.blunt.parser

import java.util.HashMap
import java.util.Map

open class TokenType<out T>(val name: String) {

    class object {
        private val keywords = HashMap<String, Keyword>()

        val foo: TypeInfo<Any> = typeinfo<String>

        fun keyword(name: String) = keywords.get(name).sure()

        val EOF               = TokenType<Unit>("<eof>")
        val LITERAL           = TokenType<Any>("<literal>")
        val IDENTIFIER        = TokenType<String>("<identifier>")
        val TYPE_OR_CTOR_NAME = TokenType<String>("<type or constructor name>")
        val OPERATOR          = TokenType<Operator>("<operator>")
        val IF = Keyword("if")
        val THEN = Keyword("then")
        val ELSE = Keyword("else")
        val LET  = Keyword("let")
        val REC  = Keyword("rec")
        val IN   = Keyword("in")
        val DATA = Keyword("data")
        val CASE = Keyword("case")
        val OF   = Keyword("of")
        val DERIVING = Keyword("deriving")

        val LAMBDA = Punctuation("\\")
        val LPAREN = Punctuation("(")
        val RPAREN = Punctuation(")")
        val SEMICOLON = Punctuation(";")
        val END = Punctuation("<end>")
        val COMMA = Punctuation(",")
        val LBRACKET = Punctuation("[")
        val RBRACKET = Punctuation("]")
        val ASSIGN = Punctuation("=")
        val UNDERSCORE = Punctuation("_")
        val OR = Punctuation("|")
        val RIGHT_ARROW = Punctuation("->")
        val BIG_RIGHT_ARROW = Punctuation("=>")

    }

    open fun toString() = name
}

class Punctuation(name: String) : TokenType<Unit>(name)

class Keyword(name: String) : TokenType<Unit>(name) {
    {
        TokenType.keywords.put(name, this)
    }

    override fun toString() = "keyword '$name'"
}
