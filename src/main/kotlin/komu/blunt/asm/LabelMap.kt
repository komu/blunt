package komu.blunt.asm

import java.util.Collections.emptySet

private class LabelMap {
    private val labelMap = hashMap<Int,MutableSet<Label>>()

    fun add(label: Label) {
        modifiableLabelSetAt(label.address).add(label)
    }

    fun labelsAt(address: Int): Set<Label> =
        labelMap[address] ?: emptySet()

    fun iterator(): Iterator<Label> =
        labelMap.values().iterator().flatMap { it.iterator() }

    private fun modifiableLabelSetAt(address: Int): MutableSet<Label> {
        val existingLabels = labelMap[address]
        if (existingLabels != null) {
            return existingLabels
        } else {
            val newLabels = hashSet<Label>()
            labelMap[address] = newLabels
            return newLabels
        }
    }
}
