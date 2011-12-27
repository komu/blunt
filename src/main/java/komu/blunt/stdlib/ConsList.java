package komu.blunt.stdlib;

import static com.google.common.base.Objects.equal;

@TypeName("[]")
public abstract class ConsList<T> {

    public static final ConsList<?> NIL = new Nil<Object>();

    private static final class Cons<T> extends ConsList<T> {
        final T head;
        final ConsList<T> tail;
        
        private Cons(T head, ConsList<T> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public T getHead() {
            return head;
        }

        @Override
        public ConsList<T> getTail() {
            return tail;
        }

        @Override
        public boolean isNil() {
            return false;
        }

        @Override
        public int hashCode() {
            return head.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) return true;
            
            if (obj instanceof Cons) {
                Cons<?> rhs = (Cons<?>) obj;

                return equal(head, rhs.head)
                    && equal(tail, rhs.tail);
            }
            
            return false;
        }
    }
    
    private static final class Nil<T> extends ConsList<T> {
        @Override
        public T getHead() {
            throw new UnsupportedOperationException("head of nil");
        }

        @Override
        public ConsList<T> getTail() {
            throw new UnsupportedOperationException("tail of nil");
        }

        @Override
        public boolean isNil() {
            return true;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Nil;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    @LibraryFunction("nil?")
    @SuppressWarnings("unused")
    public abstract boolean isNil();

    @LibraryFunction("head")
    @SuppressWarnings("unused")
    public abstract T getHead();

    @LibraryFunction("tail")
    @SuppressWarnings("unused")
    public abstract ConsList<T> getTail();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("[");

        ConsList<T> item = this;
        while (item instanceof Cons<?>) {
            Cons<T> cons = (Cons<T>) item;

            if (sb.length() > 1)
                sb.append(", ");
            
            sb.append(cons.head);
            item = cons.tail;
        }

        sb.append("]");
        return sb.toString();
    }

    @LibraryFunction("primitiveNil")
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> ConsList<T> nil() {
        return (ConsList<T>) NIL;
    }

    @LibraryFunction("primitiveCons")
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> ConsList<T> cons(T head, ConsList<T> tail) {
        return new Cons<T>(head, tail);
    }
}
