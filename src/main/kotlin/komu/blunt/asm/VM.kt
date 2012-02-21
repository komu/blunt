package komu.blunt.asm

import komu.blunt.asm.opcodes.OpCode
import komu.blunt.eval.Environment
import komu.blunt.eval.RootEnvironment
import komu.blunt.objects.Procedure

import java.util.ArrayList
import java.util.List

class VM(private val instructions: Instructions, var env: Environment, val globalEnvironment: RootEnvironment) {

    var value: Any? = null
    var procedure: Procedure? = null
    var arg: Any? = null
    var pc = 0
    var steps = 0.lng

    private val stack = ArrayList<Any?>(4096)

    fun run(): Any? {
        while (true) {
            if (pc >= instructions.count()) break
            val op = instructions.get(pc++)
            steps++
            op.execute(this)
        }

        return value
    }

    fun get(register: Register): Any? =
        when (register) {
            Register.VAL       -> value
            Register.ARG       -> arg
            Register.ENV       -> env
            Register.PC        -> pc
            Register.PROCEDURE -> procedure
            else               -> throw IllegalArgumentException("unknown register: $register")
        }

    fun set(register: Register, value: Any?) {
        when (register) {
            Register.VAL       -> this.value = value
            Register.ARG       -> arg = value
            Register.ENV       -> env = value as Environment
            Register.PC        -> pc = value as Int
            Register.PROCEDURE -> procedure = value as Procedure?
            else               -> throw IllegalArgumentException("unknown register: $register")
        }
    }

    fun push(value: Any?) {
        stack.add(value)
    }

    fun pop(): Any? = stack.remove(stack.size() - 1)
}
