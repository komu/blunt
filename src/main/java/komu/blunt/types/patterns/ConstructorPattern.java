package komu.blunt.types.patterns;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ConstructorPattern extends Pattern {
    public final String constructor;
    public final ImmutableList<Pattern> args;

    public ConstructorPattern(String constructor, ImmutableList<Pattern> args) {
        this.constructor = checkNotNull(constructor);
        this.args = checkNotNull(args);
    }

    @Override
    public String toString() {
        if (args.isEmpty())
            return constructor;
        
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(constructor);
        
        for (Pattern arg : args)
            sb.append(' ').append(arg);
        sb.append(')');

        return sb.toString();
    }
}
