package komu.blunt.parser;

import komu.blunt.ast.ASTConstant;
import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.ASTVariable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;

final class ASTMatchers {
    
    static Matcher<ASTExpression> producesExpressionMatching(final String representation) {
        return new ASTMatcher<ASTExpression>(ASTExpression.class) {
            @Override
            public boolean matches(ASTExpression exp) {
                return equal(exp.toString(), representation);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(representation);
            }
        };        
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
    static Matcher<ASTExpression> producesConstant(final Object value) {
        return new ASTMatcher<ASTConstant>(ASTConstant.class) {
            @Override
            public boolean matches(ASTConstant exp) {
                return equal(value, exp.value);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("constant ").appendValue(value);
            }
        };
    }

    private abstract static class ASTMatcher<T extends ASTExpression> extends BaseMatcher<ASTExpression> {
        
        private final Class<T> type;
        
        ASTMatcher(Class<T> type) {
            this.type = checkNotNull(type);
        }
        
        @Override
        public boolean matches(Object o) {
            return type.isInstance(o) && matches(type.cast(o));
        }
        
        protected abstract boolean matches(T exp);
    }
}
