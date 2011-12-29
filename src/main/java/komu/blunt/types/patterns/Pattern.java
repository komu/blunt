package komu.blunt.types.patterns;

public abstract class Pattern {

    public abstract <R,C> R accept(PatternVisitor<C,R> visitor, C ctx);

    @Override
    public abstract String toString();
}
