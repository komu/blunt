package komu.blunt.types.patterns;

public final class WildcardPattern extends Pattern {

    public static final WildcardPattern INSTANCE = new WildcardPattern();

    private WildcardPattern() {
    }

    @Override
    public <R, C> R accept(PatternVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        return "_";
    }
}
