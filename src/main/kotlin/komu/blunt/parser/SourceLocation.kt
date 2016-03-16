package komu.blunt.parser

class SourceLocation(val line: Int, val column: Int) {
    init {
        require(line > 0)
        require(column > 0)
    }

    override fun toString() = "$line:$column"
}

