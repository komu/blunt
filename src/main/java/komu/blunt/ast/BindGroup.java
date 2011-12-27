package komu.blunt.ast;

import komu.blunt.objects.Symbol;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static komu.blunt.utils.CollectionUtils.append;

public final class BindGroup {

    private final List<ExplicitBinding> explicitBindings = new ArrayList<ExplicitBinding>();
    private final List<List<ImplicitBinding>> implicitBindings = new ArrayList<List<ImplicitBinding>>();
    
    public BindGroup(List<ExplicitBinding> explicitBindings,
                     List<ImplicitBinding> implicitBindings) {
        this.explicitBindings.addAll(explicitBindings);
        this.implicitBindings.add(implicitBindings);
    }

    public TypeCheckResult<Assumptions> typeCheckBindGroup(TypeCheckingContext ctx) {
        Assumptions as2 = assumptionFromExplicitBindings();

        TypeCheckResult<Assumptions> res = typeCheckImplicits(ctx.extend(as2));
        Assumptions as3 = res.value;
        List<Predicate> ps = typeCheckExplicits(ctx.extend(as3.join(as2)));
        
        return new TypeCheckResult<Assumptions>(append(res.predicates, ps), as3.join(as2));
    }

    private TypeCheckResult<Assumptions> typeCheckImplicits(TypeCheckingContext ctx) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions as = new Assumptions();

        for (List<ImplicitBinding> bs : implicitBindings) {
            TypeCheckResult<Assumptions> res = ImplicitBinding.typeCheck(bs, ctx.extend(as));
            predicates.addAll(res.predicates);
            as = res.value.join(as);
        }

        return new TypeCheckResult<Assumptions>(predicates, as);
    }

    private List<Predicate> typeCheckExplicits(TypeCheckingContext ctx) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        
        for (ExplicitBinding b : explicitBindings)
            predicates.addAll(b.typeCheck(ctx));

        return predicates;
    }

    private Assumptions assumptionFromExplicitBindings() {
        Map<Symbol,Scheme> types = new HashMap<Symbol, Scheme>();
        
        for (ExplicitBinding b : explicitBindings)
            types.put(b.name, b.scheme);

        return new Assumptions(types);
    }

}
