package komu.blunt.asm

import opcodes.*
import komu.blunt.analyzer.VariableReference
import komu.blunt.core.PatternPath

/**
* Returns an instruction stream identical to this one, but which guarantees
* that given register will not be modified.
* <p>
* If the instructions in the stream will never modify given register, then
* it is safe to return the stream as it is.
*/
fun Instructions.preserving(register: Register?): Instructions {
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

fun Instructions.finishWithLinkage(linkage: Linkage?) {
    if (linkage == Linkage.NEXT) {
        // nada
    } else if (linkage == Linkage.RETURN) {
        popRegister(Register.PC)
    } else {
        jump(linkage?.label)
    }
}

fun Instructions.loadConstant(target: Register?, value: Any?) {
    this.add(OpLoadConstant(target.sure(), value))
}

fun Instructions.loadVariable(target: Register?, v: VariableReference?) {
    this.add(OpLoadVariable(target.sure(), v.sure()))
}

fun Instructions.storeVariable(v: VariableReference?, value: Register?) {
    this.add(OpStoreVariable(v, value))
}

fun Instructions.loadLambda(target: Register?, label: Label?) {
    this.add(OpLoadLambda(target.sure(), label.sure()))
}

fun Instructions.createEnvironment(envSize: Int) {
    this.add(OpCreateEnvironment(envSize))
}

fun Instructions.loadExtracted(target: Register?, source: Register?, path: PatternPath?) {
    this.add(OpLoadExtracted(target.sure(), source.sure(), path.sure()))
}

fun Instructions.loadTag(target: Register?, source: Register?, path: PatternPath?) {
    this.add(OpLoadTag(target.sure(), source.sure(), path.sure()))
}

fun Instructions.jump(label: Label?) {
    this.add(OpJump(label.sure()))
}

fun Instructions.pushLabel(label: Label?) {
    this.add(OpPushLabel(label))
}

fun Instructions.pushRegister(register: Register?) {
    this.add(OpPushRegister(register))
}

fun Instructions.popRegister(register: Register?) {
    this.add(OpPopRegister(register))
}

fun Instructions.apply() {
    this.add(OpApply.NORMAL)
}

fun Instructions.applyTail() {
    this.add(OpApply.TAIL)
}

fun Instructions.copy(target: Register?, source: Register?) {
    this.add(OpCopyRegister(target, source))
}

fun Instructions.equalConstant(target: Register?, source: Register?, value: Any?) {
    this.add(OpEqualConstant(target.sure(), source.sure(), value))
}
