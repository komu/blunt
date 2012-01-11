package komu.blunt.ast;

import komu.blunt.types.checker.Assumptions;

import java.util.ArrayList;
import java.util.List;

public final class BindGroup {

    public final List<ExplicitBinding> explicitBindings = new ArrayList<>();
    public final List<List<ImplicitBinding>> implicitBindings = new ArrayList<>();
    
    public BindGroup(List<ExplicitBinding> explicitBindings,
                     List<ImplicitBinding> implicitBindings) {
        this.explicitBindings.addAll(explicitBindings);
        this.implicitBindings.add(implicitBindings);
    }

    public Assumptions assumptionFromExplicitBindings() {
        Assumptions.Builder builder = Assumptions.builder();
        
        for (ExplicitBinding b : explicitBindings)
            builder.add(b.name, b.scheme);

        return builder.build();
    }

}
