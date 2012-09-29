package komu.blunt.parser

import komu.blunt.ast.ASTConstant
import komu.blunt.ast.ASTExpression
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

public fun producesExpressionMatching(representation: String): Matcher<ASTExpression> =
    object : ASTMatcher<ASTExpression>(javaClass<ASTExpression>()) {

        protected override fun matches(exp: ASTExpression): Boolean =
            exp.toString() == representation

        public override fun describeTo(description: Description) {
            description.appendValue(representation)
        }
    }
/*
static Matcher<ASTExpression> producesVariable(final String name) {
    return new ASTMatcher<ASTVariable>(ASTVariable.class) {
        @Override
        public boolean matches(ASTVariable exp) {
            return equal(name, exp.var.toString());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("variable ").appendValue(name);
        }
    };
}
*/
public fun producesConstant(value: Any): Matcher<ASTExpression> =
    object : ASTMatcher<ASTConstant>(javaClass<ASTConstant>()) {

        protected override fun matches(exp: ASTConstant): Boolean =
            throw UnsupportedOperationException()

        public override fun describeTo(description: Description) {
            description.appendText("constant ").appendValue(value)
        }
    }

abstract class ASTMatcher<T : ASTExpression>(val typ: Class<T>) : BaseMatcher<ASTExpression>() {

    public override fun matches(o: Any?): Boolean =
        typ.isInstance(o) && matches(typ.cast(o))

    protected abstract fun matches(exp: T): Boolean
}
