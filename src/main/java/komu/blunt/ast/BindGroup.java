package komu.blunt.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Scheme;
import komu.blunt.types.checker.Assumptions;

public final class BindGroup {

    public final List<ExplicitBinding> explicitBindings = new ArrayList<ExplicitBinding>();
    public final List<List<ImplicitBinding>> implicitBindings = new ArrayList<List<ImplicitBinding>>();
    
    public BindGroup(List<ExplicitBinding> explicitBindings,
                     List<ImplicitBinding> implicitBindings) {
        this.explicitBindings.addAll(explicitBindings);
        this.implicitBindings.add(implicitBindings);
    }

    public Assumptions assumptionFromExplicitBindings() {
        Map<Symbol,Scheme> types = new HashMap<Symbol, Scheme>();
        
        for (ExplicitBinding b : explicitBindings)
            types.put(b.name, b.scheme);

        return new Assumptions(types);
    }

}
