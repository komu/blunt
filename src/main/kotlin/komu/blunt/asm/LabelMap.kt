package komu.blunt.asm

import java.util.*
import java.util.Collections.emptySet

internal class LabelMap {
    private val labelMap = HashMap<Int,MutableSet<Label>>()

    fun add(label: Label) {
        val labels = labelMap.getOrPut(label.address) { HashSet<Label>() }
        labels.add(label)
    }

    fun labelsAt(address: Int): Set<Label> =
        labelMap[address] ?: emptySet()

    operator fun iterator(): Iterator<Label> =
        labelMap.values.asSequence().flatMap { it.asSequence() }.iterator()
}
