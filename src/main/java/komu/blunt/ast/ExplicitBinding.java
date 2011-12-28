package komu.blunt.ast;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Scheme;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ExplicitBinding {
    
    public final Symbol name;
    public final Scheme scheme;
    public final ASTExpression value;

    public ExplicitBinding(Symbol name, Scheme scheme, ASTExpression value) {
        this.name = checkNotNull(name);
        this.scheme = checkNotNull(scheme);
        this.value = checkNotNull(value);
    }
}
