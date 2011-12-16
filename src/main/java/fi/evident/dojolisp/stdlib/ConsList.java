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
        protected T getHead() {
            return head;
        }

        @Override
        protected ConsList<T> getTail() {
            return tail;
        }
    }
    
    private static final class Nil<T> extends ConsList<T> {
        @Override
        protected T getHead() {
            throw new UnsupportedOperationException("head of nil");
        }

        @Override
        protected ConsList<T> getTail() {
            throw new UnsupportedOperationException("tail of nil");
        }
    }
    
    protected abstract T getHead();
    protected abstract ConsList<T> getTail();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        ConsList<T> item = this;
        while (item instanceof Cons<?>) {
            Cons<T> cons = (Cons<T>) item;
            sb.append(sb.length() == 0 ? "[" : ", ");
            
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
    
    @LibraryFunction("head")
    public static <T> T head(ConsList<T> list) {
        return list.getHead();
    }

    @LibraryFunction("tail")
    public static <T> ConsList<T> tail(ConsList<T> list) {
        return list.getTail();
    }
}
