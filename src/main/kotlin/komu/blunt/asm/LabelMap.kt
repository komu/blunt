package komu.blunt.asm

import java.util.Collections.emptySet
import java.util.HashMap
import java.util.HashSet

private class LabelMap {
    private val labelMap = HashMap<Int,MutableSet<Label>>()

    fun add(label: Label) {
        val labels = labelMap.getOrPut(label.address) { HashSet<Label>() }
        labels.add(label)
    }

    fun labelsAt(address: Int): Set<Label> =
        labelMap[address] ?: emptySet()

    fun iterator(): Iterator<Label> =
        labelMap.values().iterator().flatMap { it.iterator() }
}
