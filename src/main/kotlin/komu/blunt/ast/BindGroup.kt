package komu.blunt.ast

import komu.blunt.types.checker.Assumptions

import java.util.ArrayList
import java.util.List

class BindGroup(explicitBindings: List<ExplicitBinding?>?, implicitBindings: List<ImplicitBinding?>?) {

    val explicitBindings: List<ExplicitBinding?> = ArrayList()
    val implicitBindings: List<List<ImplicitBinding?>?> = ArrayList();

    {
        for (val b in explicitBindings)
            this.explicitBindings.add(b)
        this.implicitBindings.add(implicitBindings)
    }

    fun assumptionFromExplicitBindings(): Assumptions {
        val builder = Assumptions.builder().sure()

        for (val b in explicitBindings)
            builder.add(b?.name.sure(), b?.scheme.sure())

        return builder.build().sure()
    }
}
