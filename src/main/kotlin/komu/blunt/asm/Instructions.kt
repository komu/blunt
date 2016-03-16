package komu.blunt.asm

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.opcodes.*
import komu.blunt.core.PatternPath
import java.util.*

class Instructions {
    private val instructions = ArrayList<OpCode>()
    private val labelMap = LabelMap()

    fun append(rhs: Instructions) {
        val relocationOffset = instructions.size

        instructions.addAll(rhs.instructions)

        for (label in rhs.labelMap) {
            label.relocateBy(relocationOffset)
            labelMap.add(label)
        }
    }

    fun modifies(register: Register): Boolean =
        instructions.any { it.modifies(register) }

    fun label(label: Label) {
        label.address = instructions.size
        labelMap.add(label)
    }

    fun dump() {
        instructions.forEachIndexed { address, instruction ->
            for (label in labelMap.labelsAt(address))
                println("$label:")

            println("    $instruction")
        }
    }

    fun add(op: OpCode) {
        instructions.add(op)
    }

    val count: Int
        get() = instructions.size

    operator fun get(pc: Int): OpCode = instructions[pc]
}

fun instructions(block: Instructions.() -> Unit): Instructions {
    val instructions = Instructions()
    instructions.block()
    return instructions
}

fun Instructions.instructionsOf(instructions: Instructions) {
    append(instructions)
}

/**
* Returns an instruction stream identical to this one, but which guarantees
* that given register will not be modified.
* <p>
* If the instructions in the stream will never modify given register, then
* it is safe to return the stream as it is.
*/
fun Instructions.preserving(register: Register, block: Instructions.() -> Unit) {
    val instructions = Instructions()
    instructions.block()
    if (instructions.modifies(register)) {
        pushRegister(register)
        instructionsOf(instructions)
        popRegister(register)
    } else {
        instructionsOf(instructions)
    }
}

fun Instructions.finishWithLinkage(linkage: Linkage) {
    when (linkage) {
        Linkage.NEXT   -> { }
        Linkage.RETURN -> popRegister(Register.PC)
        else           -> jump(linkage.label!!)
    }
}

fun Instructions.jumpIfFalse(register: Register, label: Label) {
    this.add(OpJumpIfFalse(register, label))
}

fun Instructions.loadConstant(target: Register, value: Any) {
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

fun Instructions.equalConstant(target: Register, source: Register, value: Any) {
    this.add(OpEqualConstant(target, source, value))
}
