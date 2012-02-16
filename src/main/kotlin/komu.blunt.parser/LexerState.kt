package komu.blunt.parser

import java.util.List

class LexerState(val position: Int, val indents: List<Int>, val nextToken: Token<out Any?>?)
