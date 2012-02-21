package komu.blunt.eval

import komu.blunt.analyzer.VariableReference

import java.util.Arrays

import java.lang.Math.max

class RootEnvironment : Environment() {

    private var bindings = Array<Any?>(512)

    override fun set(frame: Int, offset: Int, value: Any?) {
        checkFrame(frame)

        bindings[offset] = value
    }

    override fun lookup(frame: Int, offset: Int): Any? {
        checkFrame(frame)

        return bindings[offset]
    }

    fun define(v: VariableReference, value: Any?) {
        checkFrame(v.frame)

        if (v.offset >= bindings.size)
            bindings = Arrays.copyOf(bindings, max(v.offset + 1, bindings.size * 2)).sure()

        bindings[v.offset] = value
    }

    private fun checkFrame(frame: Int) {
        if (frame != 0 && frame != VariableReference.GLOBAL_FRAME)
            throw IllegalArgumentException("invalid frame $frame");
    }
}
