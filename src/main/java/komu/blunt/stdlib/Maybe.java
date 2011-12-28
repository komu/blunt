package komu.blunt.stdlib;

import com.google.common.base.Objects;

@TypeName("Maybe")
public abstract class Maybe<T> {

    @LibraryValue("Nothing")
    @SuppressWarnings("unused")
    public static final Maybe<?> NOTHING = new Nothing<Object>();

    private static final class Just<T> extends Maybe<T> {
        private final T value;

        private Just(T value) {
            this.value = value;
        }

        @Override
        public boolean isJust() {
            return true;
        }

        @Override
        public T fromJust() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            
            if (obj instanceof Just<?>) {
                Just<?> rhs = (Just<?>) obj;
                return Objects.equal(value, rhs.value);
            }
            
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "(Just " + value + ")";
        }
    }

    private static final class Nothing<T> extends Maybe<T> {
        @Override
        public boolean isJust() {
            return false;
        }

        @Override
        public T fromJust() {
            return BasicFunctions.error("can't extract type from Nothing");
        }

        @Override
        public String toString() {
            return "Nothing";
        }
    }

    @LibraryFunction("just?")
    @SuppressWarnings("unused")
    public abstract boolean isJust();

    @LibraryFunction("nothing?")
    @SuppressWarnings("unused")
    public final boolean isNothing() {
        return !isJust();
    }

    @LibraryFunction("fromJust")
    @SuppressWarnings("unused")
    public abstract T fromJust();

    @LibraryFunction("Just")
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> Maybe<T> just(T value) {
        return new Just<T>(value);
    }
}
