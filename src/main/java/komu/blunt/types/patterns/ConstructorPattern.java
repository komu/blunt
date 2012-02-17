package komu.blunt.types.patterns;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorPattern extends Pattern {
    public final String name;
    public final ImmutableList<Pattern> args;

    ConstructorPattern(String name, ImmutableList<Pattern> args) {
        this.name = checkNotNull(name);
        this.args = checkNotNull(args);
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

    @Override
    public int hashCode() {
        return name.hashCode() * 79 + args.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof ConstructorPattern) {
            ConstructorPattern rhs = (ConstructorPattern) obj;

            return name.equals(rhs.name)
                && args.equals(rhs.args);
        }

        return false;
    }
}
