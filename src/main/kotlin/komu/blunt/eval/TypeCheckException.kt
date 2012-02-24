package komu.blunt.eval

import komu.blunt.analyzer.AnalyzationException
import komu.blunt.types.checker.UnificationException

class TypeCheckException(message: String) : AnalyzationException(message) {
    this(e: UnificationException): this(e.getMessage().sure()) { }
}
