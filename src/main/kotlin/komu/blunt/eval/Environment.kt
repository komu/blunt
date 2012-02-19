package komu.blunt.eval;

import com.google.common.base.Preconditions.checkArgument
import komu.blunt.analyzer.VariableReference
import java.util.Arrays

class NestedEnvironment(private val bindings: Array<Any?>, private val parent: Environment) : Environment() {

    override fun set(frame: Int, offset: Int, value: Any?) {
        if (frame == 0)
            bindings[offset] = value
        else
            parent.set(frame-1, offset, value)
    }

    override fun lookup(frame: Int, offset: Int): Any? =
        if (frame == 0)
            bindings[offset]
        else
            parent.lookup(frame-1, offset)
}

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
            bindings = Arrays.copyOf(bindings, Math.max(v.offset + 1, bindings.size * 2)).sure()

        bindings[v.offset] = value
    }

    private fun checkFrame(frame: Int) {
        if (frame != 0 && frame != VariableReference.GLOBAL_FRAME)
            throw IllegalArgumentException("invalid frame $frame")
    }
}

