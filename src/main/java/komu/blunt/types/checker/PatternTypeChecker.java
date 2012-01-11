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
        TypeVariable tv = tc.newTVar();

        return new PatternTypeCheckResult<Type>(Assumptions.singleton(pattern.var, tv.toScheme()), tv);
    }

    @Override
    public PatternTypeCheckResult<Type> visit(WildcardPattern pattern, Void ctx) {
        TypeVariable tv = tc.newTVar();

        return new PatternTypeCheckResult<Type>(Assumptions.empty(), tv);
    }

    @Override
    public PatternTypeCheckResult<Type> visit(LiteralPattern pattern, Void ctx) {
        Type type = Type.fromClass(pattern.value.getClass());

        return new PatternTypeCheckResult<>(Assumptions.empty(), type);
    }
    
    @Override
    public PatternTypeCheckResult<Type> visit(ConstructorPattern pattern, Void ctx) {
        ConstructorDefinition constructor = tc.findConstructor(pattern.name);

        if (pattern.args.size() != constructor.arity)
            throw new TypeCheckException("invalid amount of arguments for constructor '" + pattern.name + "';" +
                    " expected " + constructor.arity + ", but got " + pattern.args.size());

        PatternTypeCheckResult<List<Type>> result = assumptionsFrom(pattern.args);
        Type type = tc.newTVar();

        Qualified<Type> q = tc.freshInstance(constructor.scheme);
        
        tc.unify(q.value, functionType(result.value, type));

        List<Predicate> predicates = new ArrayList<>();
        predicates.addAll(result.predicates);
        predicates.addAll(q.predicates);

        return new PatternTypeCheckResult<>(predicates, result.as, type);
    }

    PatternTypeCheckResult<List<Type>> assumptionsFrom(List<Pattern> patterns) {
        List<Predicate> predicates = new ArrayList<>();
        Assumptions as = Assumptions.empty();
        List<Type> types = new ArrayList<>(patterns.size());

        for (Pattern pattern : patterns) {
            PatternTypeCheckResult<Type> result = tc.typeCheck(pattern);
            predicates.addAll(result.predicates);
            as = as.join(result.as);
            types.add(result.value);
        }
        
        return new PatternTypeCheckResult<>(predicates, as, types);
    }
}
