package komu.blunt.types.patterns;

public final class WildcardPattern extends Pattern {

    static final WildcardPattern INSTANCE = new WildcardPattern();

    private WildcardPattern() {
    }

    @Override
    public String toString() {
        return "_";
    }
}
