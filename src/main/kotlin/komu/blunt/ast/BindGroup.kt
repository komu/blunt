package komu.blunt.ast

import java.util.ArrayList
import komu.blunt.types.checker.Assumptions

class BindGroup(explicitBindings: List<ExplicitBinding>, implicitBindings: List<ImplicitBinding>) {

    val explicitBindings: MutableList<ExplicitBinding> = ArrayList(explicitBindings)
    val implicitBindings: MutableList<List<ImplicitBinding>> = ArrayList(listOf(implicitBindings))

    fun assumptionFromExplicitBindings(): Assumptions {
        val builder = Assumptions.builder()

        for (binding in explicitBindings)
            builder[binding.name] = binding.scheme

        return builder.build()
    }
}
