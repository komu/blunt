package fi.evident.dojolisp.types;

public abstract class Kind {
    
    public static final Kind STAR = new Kind() {
        @Override
        public String toString(boolean l) {
            return "*";
        }
    };

    Kind() { }  // only allow subclasses in this package

    @Override
    public String toString() {
        return toString(false);
    }

    protected abstract String toString(boolean l);
    
    public static Kind ofParams(int count) {
        if (count < 0) throw new IllegalArgumentException("negative count: " + count);
        
        if (count == 0)
            return STAR;
        else
            return arrow(STAR, Kind.ofParams(count-1));
    }

    public static Kind arrow(Kind left, Kind right) {
        return new ArrowKind(left, right);
    }
}
