package fi.evident.dojolisp.types;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

final class ArrowKind extends Kind {
    final Kind left;
    final Kind right;

    ArrowKind(Kind left, Kind right) {
        this.left = requireNonNull(left);
        this.right = requireNonNull(right);
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
    public String toString(boolean l) {
        if (l) {
            return "(" + left.toString(true) + " -> " + right.toString(false) + ")";
        } else {
            return left.toString(true) + " -> " + right.toString(false);
        }
    }
}
