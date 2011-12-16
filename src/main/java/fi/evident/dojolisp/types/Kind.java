package fi.evident.dojolisp.types;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public abstract class Kind {
    
    public static final Kind STAR = new Kind() {
        @Override
        public String toString() {
            return "*";
        }
    };
    
    public static Kind arrow(Kind left, Kind right) {
        return new ArrowKind(left, right);
    }

    private static final class ArrowKind extends Kind {
        private final Kind left;
        private final Kind right;

        private ArrowKind(Kind left, Kind right) {
            this.right = requireNonNull(right);
            this.left = requireNonNull(left);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;

            if (obj instanceof ArrowKind) {
                ArrowKind rhs = (ArrowKind) obj;

                return left.equals(rhs.left)
                    && right.equals(rhs.right);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return left.hashCode() * 79 + right.hashCode();
        }

        @Override
        public String toString() {
            return "(" + left + " -> " + right + ")";
        }
    }
}
