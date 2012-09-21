package komu.blunt.ast

import komu.blunt.types.checker.Assumptions

class BindGroup(explicitBindings: List<ExplicitBinding>, implicitBindings: List<ImplicitBinding>) {

    public val explicitBindings: MutableList<ExplicitBinding> = arrayList()
    public val implicitBindings: MutableList<List<ImplicitBinding>> = arrayList();

    {
        this.explicitBindings.addAll(explicitBindings)
        this.implicitBindings.add(implicitBindings)
    }

    fun assumptionFromExplicitBindings(): Assumptions {
        val builder = Assumptions.builder()

        for (val b in explicitBindings)
            builder.add(b.name, b.scheme)

        return builder.build()
    }
}
