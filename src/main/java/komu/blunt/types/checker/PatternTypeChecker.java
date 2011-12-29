package komu.blunt.types.checker;

import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.*;
import komu.blunt.types.patterns.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Type.functionType;

final class PatternTypeChecker implements PatternVisitor<Void,PatternTypeCheckResult<Type>> {

    private final TypeChecker tc;

    public PatternTypeChecker(TypeChecker tc) {
        this.tc = checkNotNull(tc);
    }

    @Override
    public PatternTypeCheckResult<Type> visit(VariablePattern pattern, Void ctx) {
        TypeVariable tv = tc.newTVar(Kind.STAR);

        return new PatternTypeCheckResult<Type>(Assumptions.singleton(pattern.var, tv.toScheme()), tv);
    }

    @Override
    public PatternTypeCheckResult<Type> visit(WildcardPattern pattern, Void ctx) {
        TypeVariable tv = tc.newTVar(Kind.STAR);

        return new PatternTypeCheckResult<Type>(Assumptions.empty(), tv);
    }

    @Override
    public PatternTypeCheckResult<Type> visit(LiteralPattern pattern, Void ctx) {
        Type type = Type.fromClass(pattern.value.getClass());

        return new PatternTypeCheckResult<Type>(Assumptions.empty(), type);
    }
    
    @Override
    public PatternTypeCheckResult<Type> visit(ConstructorPattern pattern, Void ctx) {
        ConstructorDefinition constructor = tc.findConstructor(pattern.name);

        if (pattern.args.size() != constructor.arity)
            throw new TypeCheckException("invalid amount of arguments for constructor");

        PatternTypeCheckResult<List<Type>> result = typeCheckPatterns(pattern.args);
        Type type = tc.newTVar(Kind.STAR);

        Qualified<Type> q = tc.freshInstance(constructor.scheme);
        
        tc.unify(q.value, functionType(result.value, type));

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.addAll(result.predicates);
        predicates.addAll(q.predicates);

        return new PatternTypeCheckResult<Type>(predicates, result.as, type);
    }

    private PatternTypeCheckResult<List<Type>> typeCheckPatterns(List<Pattern> patterns) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions as = Assumptions.empty();
        List<Type> types = new ArrayList<Type>(patterns.size());

        for (Pattern pattern : patterns) {
            PatternTypeCheckResult<Type> result = tc.typeCheck(pattern);
            predicates.addAll(result.predicates);
            as = as.join(result.as);
            types.add(result.value);
        }
        
        return new PatternTypeCheckResult<List<Type>>(predicates, as, types);
    }
}
