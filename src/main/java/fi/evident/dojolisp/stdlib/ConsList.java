package fi.evident.dojolisp.stdlib;

@SuppressWarnings("unused")
public abstract class ConsList<T> {
    
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
    }

    @LibraryFunction("nil?")
    public abstract boolean isNil();

    @LibraryFunction("head")
    public abstract T getHead();

    @LibraryFunction("tail")
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

    @LibraryFunction("nil")
    public static <T> ConsList<T> nil() {
        return new Nil<T>();
    }

    @LibraryFunction("cons")
    public static <T> ConsList<T> cons(T head, ConsList<T> tail) {
        return new Cons<T>(head, tail);
    }
}
