package komu.blunt.parser

import komu.blunt.ast.ASTConstant
import komu.blunt.ast.ASTExpression
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

public fun producesExpressionMatching(representation: String): Matcher<ASTExpression> =
    object : ASTMatcher<ASTExpression>(javaClass<ASTExpression>()) {

        override fun matches(exp: ASTExpression) =
            exp.toString() == representation

        override fun describeTo(description: Description) {
            description.appendValue(representation)
        }
    }

public fun producesConstant(value: Any): Matcher<ASTExpression> =
    object : ASTMatcher<ASTConstant>(javaClass<ASTConstant>()) {

        override fun matches(exp: ASTConstant) =
            value == exp.value

        override fun describeTo(description: Description) {
            description.appendText("constant ").appendValue(value)
        }
    }

abstract class ASTMatcher<T : ASTExpression>(val typ: Class<T>) : BaseMatcher<ASTExpression>() {

    override fun matches(o: Any?): Boolean =
        typ.isInstance(o) && matches(typ.cast(o)!!)

    protected abstract fun matches(exp: T): Boolean
}
