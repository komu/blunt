package komu.blunt.types.checker;

import komu.blunt.ast.ASTExpression;
import komu.blunt.ast.BindGroup;
import komu.blunt.ast.ExplicitBinding;
import komu.blunt.ast.ImplicitBinding;
import komu.blunt.types.*;
import komu.blunt.utils.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.ast.ImplicitBinding.bindingNames;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.toSchemes;
import static komu.blunt.types.checker.TypeUtils.getTypeVariables;
import static komu.blunt.utils.CollectionUtils.intersection;

final class BindingTypeChecker {
    
    private final TypeChecker tc;

    BindingTypeChecker(TypeChecker tc) {
        this.tc = checkNotNull(tc);
    }

    public TypeCheckResult<Assumptions> typeCheckBindGroup(BindGroup bindings, Assumptions as) {
        Assumptions as2 = bindings.assumptionFromExplicitBindings();

        TypeCheckResult<Assumptions> res = typeCheckImplicits(bindings, as.join(as2));
        Assumptions as3 = res.value;
        List<Predicate> ps = typeCheckExplicits(bindings, as.join(as3.join(as2)));

        return TypeCheckResult.of(as3.join(as2), res.predicates, ps);
    }

    private TypeCheckResult<Assumptions> typeCheckImplicits(BindGroup bindings, Assumptions origAs) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions as = Assumptions.empty();

        for (List<ImplicitBinding> bs : bindings.implicitBindings) {
            TypeCheckResult<Assumptions> res = typeCheckImplicitGroup(bs, as.join(origAs));
            predicates.addAll(res.predicates);
            as = res.value.join(as);
        }

        return TypeCheckResult.of(as, predicates);
    }

    private List<Predicate> typeCheckExplicits(BindGroup bindGroup, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (ExplicitBinding b : bindGroup.explicitBindings)
            predicates.addAll(typeCheck(b, as));

        return predicates;
    }

    private List<Predicate> typeCheck(ExplicitBinding binding, Assumptions as) {
        throw new UnsupportedOperationException("explicit bindings are not implemented");
    }

    private TypeCheckResult<Assumptions> typeCheckImplicitGroup(List<ImplicitBinding> bindings, Assumptions as) {
        List<Type> typeVariables = tc.newTVars(bindings.size(), Kind.STAR);
        List<Predicate> predicates = typeCheckAndUnifyBindings(bindings, typeVariables, as);

        List<Type> types = tc.applySubstitution(typeVariables);
        Set<TypeVariable> fs = TypeUtils.getTypeVariables(tc.applySubstitution(as));
        
        List<Set<TypeVariable>> vss = new ArrayList<Set<TypeVariable>>(types.size());
        Set<TypeVariable> genericVariables = new HashSet<TypeVariable>();
        for (Type t : types) {
            Set<TypeVariable> vars = getTypeVariables(t);
            vss.add(vars);
            genericVariables.addAll(vars);
        }
        
        genericVariables.removeAll(fs);
        Set<TypeVariable> sharedVariables = intersection(vss);

        Pair<List<Predicate>, List<Predicate>> split = tc.classEnv.split(fs, sharedVariables, predicates);
        List<Predicate> deferredPredicates = split.first;
        List<Predicate> retainedPredicates = split.second;

        List<Scheme> finalSchemes = new ArrayList<Scheme>(types.size());
        for (Type t : types)
            finalSchemes.add(quantify(genericVariables, new Qualified<Type>(retainedPredicates, t)));

        Assumptions finalAssumptions = Assumptions.from(bindingNames(bindings), finalSchemes);
        return TypeCheckResult.of(finalAssumptions, deferredPredicates);
    }

    private List<Predicate> typeCheckAndUnifyBindings(List<ImplicitBinding> bs, List<Type> ts, Assumptions as) {
        Assumptions as2 = Assumptions.from(bindingNames(bs), toSchemes(ts)).join(as);

        List<Predicate> predicates = new ArrayList<Predicate>();

        for (int i = 0; i < ts.size(); i++) {
            ASTExpression exp = bs.get(i).expr;
            Type type = ts.get(i);

            TypeCheckResult<Type> res = tc.typeCheck(exp, as2);
            tc.unify(res.value, type);
            predicates.addAll(res.predicates);
        }

        return tc.applySubstitution(predicates);
    }
}
