package fi.evident.dojolisp.stdlib;

public final class ConsList<T> {
    final T head;
    final ConsList<T> tail;
    
    private ConsList(T head, ConsList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    @LibraryFunction("nil")
    public static <T> ConsList<T> nil() {
        return null;
    }

    @LibraryFunction("cons")
    public static <T> ConsList<T> cons(T head, ConsList<T> tail) {
        return new ConsList<T>(head, tail);
    }
    
    @LibraryFunction("head")
    public static <T> T head(ConsList<T> list) {
        return list.head;
    }

    @LibraryFunction("tail")
    public static <T> ConsList<T> tail(ConsList<T> list) {
        return list.tail;
    }
}
