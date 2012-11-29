package komu.blunt.parser

class SourceLocation(val line: Int, val column: Int) {
    {
        require(line > 0)
        require(column > 0)
    }

    fun toString() = "$line:$column"
}

