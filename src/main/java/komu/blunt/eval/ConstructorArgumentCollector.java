package komu.blunt.eval;

import com.google.common.collect.ImmutableList;
import komu.blunt.objects.PrimitiveProcedure;
import komu.blunt.objects.TypeConstructorValue;
import komu.blunt.types.ConstructorDefinition;

import static com.google.common.base.Preconditions.checkNotNull;

final class ConstructorArgumentCollector implements PrimitiveProcedure {
    
    private final ConstructorDefinition ctor;
    private final ImmutableList<Object> args;

    public static Object createConstructor(ConstructorDefinition ctor) {
        if (ctor.arity == 0)
            return new TypeConstructorValue(ctor.index, ctor.name);
        else
            return new ConstructorArgumentCollector(ctor, ImmutableList.of());
    }
    
    private ConstructorArgumentCollector(ConstructorDefinition ctor, ImmutableList<Object> args) {
        this.args = args;
        this.ctor = checkNotNull(ctor);
    }

    @Override
    public Object apply(Object arg) {
        ImmutableList<Object> newArgs = ImmutableList.builder().addAll(args).add(arg).build();
        if (newArgs.size() == ctor.arity)
            return new TypeConstructorValue(ctor.index, ctor.name, newArgs.toArray());
        else
            return new ConstructorArgumentCollector(ctor, newArgs);
    }
}
