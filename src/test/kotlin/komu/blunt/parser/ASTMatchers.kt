package komu.blunt.parser

import komu.blunt.ast.ASTExpression
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

fun producesExpressionMatching(representation: String): Matcher<ASTExpression> =
    astExpression<ASTExpression>(representation) { it.toString() == representation }

fun producesConstant(value: Any): Matcher<ASTExpression> =
    astExpression<ASTExpression.Constant>("constant $value") { value == it.value }

private inline fun <reified T : ASTExpression> astExpression(desc: String, crossinline predicate: (T) -> Boolean): BaseMatcher<ASTExpression> = object : BaseMatcher<ASTExpression>() {
    override fun matches(o: Any?) = o is T && predicate(o)
    override fun describeTo(description: Description) { description.appendValue(desc) }
}
