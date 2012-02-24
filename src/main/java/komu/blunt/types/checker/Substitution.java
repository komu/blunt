package komu.blunt.types.checker;

import com.google.common.collect.ImmutableMap;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Substitution {
    
    // @@
    public abstract Substitution compose(Substitution s2);
    public abstract Substitution merge(Substitution s2);
    public abstract Substitution apply(Substitution subst);
    public abstract Type lookup(TypeVariable variable);
    public abstract List<Type> apply(List<Type> types);
}
