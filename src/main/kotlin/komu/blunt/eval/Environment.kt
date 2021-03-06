package komu.blunt.eval

import komu.blunt.analyzer.VariableReference
import java.lang.Math.max
import java.util.*

abstract class Environment {

    operator fun get(v: VariableReference) =
        get(v.frame, v.offset)

    operator fun set(v: VariableReference, value: Any?) {
        set(v.frame, v.offset, value)
    }

    abstract operator fun get(frame: Int, offset: Int): Any?
    abstract operator fun set(frame: Int, offset: Int, value: Any?)

    fun extend(envSize: Int, arg: Any?): Environment {
        val args = Array<Any?>(envSize) { null }
        args[0] = arg
        return NestedEnvironment(args, this)
    }

    fun extend(envSize: Int): Environment =
        NestedEnvironment(Array(envSize) { null }, this)
}

class RootEnvironment : Environment() {

    private var bindings = Array<Any?>(512) { null }

    override fun set(frame: Int, offset: Int, value: Any?) {
        checkFrame(frame)

        bindings[offset] = value
    }

    override fun get(frame: Int, offset: Int): Any? {
        checkFrame(frame)

        return bindings[offset]
    }

    fun define(v: VariableReference, value: Any?) {
        checkFrame(v.frame)

        if (v.offset >= bindings.size)
            bindings = Arrays.copyOf(bindings, max(v.offset + 1, bindings.size * 2))

        bindings[v.offset] = value
    }

    private fun checkFrame(frame: Int) {
        if (frame != 0 && frame != VariableReference.GLOBAL_FRAME)
            throw IllegalArgumentException("invalid frame $frame");
    }
}

class NestedEnvironment(private val bindings: Array<Any?>, private val parent: Environment) : Environment() {

    override fun get(frame: Int, offset: Int): Any? =
        if (frame == 0)
            bindings[offset]
        else
            parent[frame-1, offset]

    override fun set(frame: Int, offset: Int, value: Any?) {
        if (frame == 0)
            bindings[offset] = value
        else
            parent[frame-1, offset] = value
    }
}
