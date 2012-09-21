package komu.blunt.parser

class SourceLocation(val line: Int, val column: Int) {
    {
        check(line > 0)
        check(column > 0)
    }

    fun toString() = "$line:$column"
}

