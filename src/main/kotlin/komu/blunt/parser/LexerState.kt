package komu.blunt.parser

class LexerState(val position: Int, val indents: List<Int>, val nextToken: Token<Any>?)
