package komu.blunt.types;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeCheckingContext {
    
    public final ClassEnv ce;
    public final TypeChecker tc;
    public final Assumptions as;

    public TypeCheckingContext(ClassEnv ce, TypeChecker tc, Assumptions as) {
        this.ce = checkNotNull(ce);
        this.tc = checkNotNull(tc);
        this.as = checkNotNull(as);
    }
}
