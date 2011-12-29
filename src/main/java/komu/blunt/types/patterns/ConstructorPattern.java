package komu.blunt.types.patterns;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorPattern extends Pattern {
    public final String name;
    public final ImmutableList<Pattern> args;

    public ConstructorPattern(String name, ImmutableList<Pattern> args) {
        this.name = checkNotNull(name);
        this.args = checkNotNull(args);
    }

    @Override
    public <R, C> R accept(PatternVisitor<C, R> visitor, C ctx) {
        return visitor.visit(this, ctx);
    }

    @Override
    public String toString() {
        if (args.isEmpty())
            return name;
        
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(name);
        
        for (Pattern arg : args)
            sb.append(' ').append(arg);
        sb.append(')');

        return sb.toString();
    }
}
