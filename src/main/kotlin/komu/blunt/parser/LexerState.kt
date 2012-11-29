package komu.blunt.parser

class LexerState(val readerState: SourceReaderState, val indents: List<Int>, val nextToken: Token<Any>?)
