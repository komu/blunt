package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import komu.blunt.objects.Symbol;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Predicate;
import komu.blunt.types.Scheme;
import komu.blunt.types.checker.Assumptions;
import komu.blunt.types.checker.TypeChecker;

public final class ExplicitBinding {
    
    public final Symbol name;
    public final Scheme scheme;
    public final ASTExpression value;

    public ExplicitBinding(Symbol name, Scheme scheme, ASTExpression value) {
        this.name = checkNotNull(name);
        this.scheme = checkNotNull(scheme);
        this.value = checkNotNull(value);
    }

    public List<Predicate> typeCheck(ClassEnv classEnv, TypeChecker typeChecker, final Assumptions as) {
        throw new UnsupportedOperationException("explicit bindings are not implemented");
    }
}
