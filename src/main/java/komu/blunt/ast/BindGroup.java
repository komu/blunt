package komu.blunt.ast;

import static komu.blunt.utils.CollectionUtils.append;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.Predicate;
import komu.blunt.types.Scheme;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeCheckingContext;

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

        TypeCheckResult<Assumptions> res = typeCheckImplicits(new TypeCheckingContext(ctx.ce, ctx.tc, as2.join(ctx.as)));
        Assumptions as3 = res.value;
        List<Predicate> ps = typeCheckExplicits(new TypeCheckingContext(ctx.ce, ctx.tc, as3.join(as2).join(ctx.as)));
        
        return new TypeCheckResult<Assumptions>(append(res.predicates, ps), as3.join(as2));
    }

    private TypeCheckResult<Assumptions> typeCheckImplicits(TypeCheckingContext ctx) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions assumptions = ctx.as;

        for (List<ImplicitBinding> bs : implicitBindings) {
            TypeCheckResult<Assumptions> res = ImplicitBinding.typeCheck(bs, ctx.ce, ctx.tc, assumptions);
            predicates.addAll(res.predicates);
            assumptions = res.value.join(assumptions);
        }

        return new TypeCheckResult<Assumptions>(predicates, assumptions);
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
