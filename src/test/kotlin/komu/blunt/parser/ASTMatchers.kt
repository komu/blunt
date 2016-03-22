package komu.blunt.parser

import komu.blunt.ast.ASTExpression
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun producesExpressionMatching(representation: String): Matcher<ASTExpression> =
    object : ASTMatcher<ASTExpression>(ASTExpression::class.java) {

        override fun matches(exp: ASTExpression) =
            exp.toString() == representation

        override fun describeTo(description: Description) {
            description.appendValue(representation)
        }
    }

fun producesConstant(value: Any): Matcher<ASTExpression> =
    object : ASTMatcher<ASTExpression.Constant>(ASTExpression.Constant::class.java) {

        override fun matches(exp: ASTExpression.Constant) =
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
