package komu.blunt.parser

import com.google.common.base.Preconditions.checkArgument

class SourceLocation(val line: Int, val column: Int) {
    {
        checkArgument(line > 0)
        checkArgument(column > 0)
    }

    fun toString() = "$line:$column"
}

