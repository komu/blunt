package fi.evident.dojolisp.utils;

public class Objects {
    
    public static <T> T requireNonNull(T value) {
        if (value == null) throw new NullPointerException();

        return value;
    }

    public static boolean equal(Object x, Object y) {
        return x == null ? y == null : x.equals(y);
    }
}
