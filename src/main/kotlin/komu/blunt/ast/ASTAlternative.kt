package komu.blunt.ast

import komu.blunt.types.patterns.Pattern

class ASTAlternative(val pattern: Pattern, val value: ASTExpression) {
    fun toString() = "$pattern -> $value"
    fun simplify() = ASTAlternative(pattern, value.simplify())
}
