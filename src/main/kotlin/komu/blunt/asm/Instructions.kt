package komu.blunt.asm

import opcodes.*
import komu.blunt.analyzer.VariableReference
import komu.blunt.core.PatternPath
import java.util.Set
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import std.util.*

class Instructions {
    private val instructions = ArrayList<OpCode>()
    private val labelMap = HashMap<Int,Set<Label>>()

    fun append(rhs: Instructions) {
        val relocationOffset = instructions.size()

        instructions.addAll(rhs.instructions)

        for (val labels in rhs.labelMap.values()) {
            for (val label in labels) {
                label.relocateBy(relocationOffset)
                getLabels(label.address).add(label)
            }
        }
    }

    fun modifies(register: Register): Boolean =
        instructions any { it.modifies(register) }

    fun label(label: Label) {
        label.address = instructions.size()
        getLabels(label.address).add(label)
    }

    private fun getLabels(address: Int): Set<Label> {
        var labels = labelMap.get(address)
        if (labels != null) {
            labels = HashSet()
            labelMap.put(address, labels.sure())
        }
        return labels.sure()
    }

    fun dump() {
        var address = 0;
        for (val instruction in instructions) {
            for (val label in getLabels(address++))
                println("$label:")

            println("    $instruction")
        }
    }

    fun add(op: OpCode) {
        instructions.add(op)
    }

    fun count() = instructions.size()

    fun get(pc: Int): OpCode = instructions[pc]

    fun pos(): Int = instructions.size()
}

/**
* Returns an instruction stream identical to this one, but which guarantees
* that given register will not be modified.
* <p>
* If the instructions in the stream will never modify given register, then
* it is safe to return the stream as it is.
*/
fun Instructions.preserving(register: Register): Instructions {
    if (modifies(register)) {
        val instructions = Instructions()
        instructions.pushRegister(register)
        instructions.append(this)
        instructions.popRegister(register)
        return instructions
    } else {
        return this
    }
}

fun Instructions.finishWithLinkage(linkage: Linkage) {
    if (linkage == Linkage.NEXT) {
        // nada
    } else if (linkage == Linkage.RETURN) {
        popRegister(Register.PC)
    } else {
        jump(linkage.label.sure())
    }
}

fun Instructions.jumpIfFalse(register: Register, label: Label) {
    this.add(OpJumpIfFalse(register, label))
}

fun Instructions.loadConstant(target: Register, value: Any?) {
    this.add(OpLoadConstant(target, value))
}

fun Instructions.loadVariable(target: Register, v: VariableReference) {
    this.add(OpLoadVariable(target, v))
}

fun Instructions.storeVariable(v: VariableReference, value: Register) {
    this.add(OpStoreVariable(v, value))
}

fun Instructions.loadLambda(target: Register, label: Label) {
    this.add(OpLoadLambda(target, label))
}

fun Instructions.createEnvironment(envSize: Int) {
    this.add(OpCreateEnvironment(envSize))
}

fun Instructions.loadExtracted(target: Register, source: Register, path: PatternPath) {
    this.add(OpLoadExtracted(target, source, path))
}

fun Instructions.loadTag(target: Register, source: Register, path: PatternPath) {
    this.add(OpLoadTag(target, source, path))
}

fun Instructions.jump(label: Label) {
    this.add(OpJump(label))
}

fun Instructions.pushLabel(label: Label) {
    this.add(OpPushLabel(label))
}

fun Instructions.pushRegister(register: Register) {
    this.add(OpPushRegister(register))
}

fun Instructions.popRegister(register: Register) {
    this.add(OpPopRegister(register))
}

fun Instructions.apply() {
    this.add(OpApply.NORMAL)
}

fun Instructions.applyTail() {
    this.add(OpApply.TAIL)
}

fun Instructions.copy(target: Register, source: Register) {
    this.add(OpCopyRegister(target, source))
}

fun Instructions.equalConstant(target: Register, source: Register, value: Any?) {
    this.add(OpEqualConstant(target, source, value))
}
