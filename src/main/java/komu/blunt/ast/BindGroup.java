package komu.blunt.ast;

import komu.blunt.objects.Symbol;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static komu.blunt.utils.ListUtils.append;

public final class BindGroup {

    private final List<ExplicitBinding> explicitBindings = new ArrayList<ExplicitBinding>();
    private final List<List<ImplicitBinding>> implicitBindings = new ArrayList<List<ImplicitBinding>>();
    
    public BindGroup(List<ExplicitBinding> explicitBindings,
                     List<ImplicitBinding> implicitBindings) {
        this.explicitBindings.addAll(explicitBindings);
        this.implicitBindings.add(implicitBindings);
    }

    public TypeCheckResult<Assumptions> typeCheckBindGroup(ClassEnv ce, TypeChecker tc, Assumptions as) {
        Assumptions as2 = assumptionFromExplicitBindings();

        TypeCheckResult<Assumptions> res = typeCheckImplicits(ce, tc, as2.join(as));
        Assumptions as3 = res.value;
        List<Predicate> ps = typeCheckExplicits(ce, tc, as3.join(as2).join(as));
        
        return new TypeCheckResult<Assumptions>(append(res.predicates, ps), as3.join(as2));
    }

    private TypeCheckResult<Assumptions> typeCheckImplicits(ClassEnv ce, TypeChecker tc, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions assumptions = as;

        for (List<ImplicitBinding> bs : implicitBindings) {
            TypeCheckResult<Assumptions> res = ImplicitBinding.typeCheck(bs, ce, tc, assumptions);
            predicates.addAll(res.predicates);
            assumptions = res.value.join(assumptions);
        }

        return new TypeCheckResult<Assumptions>(predicates, assumptions);
    }

    private List<Predicate> typeCheckExplicits(ClassEnv ce, TypeChecker tc, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        
        for (ExplicitBinding b : explicitBindings)
            predicates.addAll(b.typeCheck(ce, tc, as));

        return predicates;
    }

    private Assumptions assumptionFromExplicitBindings() {
        Map<Symbol,Scheme> types = new HashMap<Symbol, Scheme>();
        
        for (ExplicitBinding b : explicitBindings)
            types.put(b.name, b.scheme);

        return new Assumptions(types);
    }

}
