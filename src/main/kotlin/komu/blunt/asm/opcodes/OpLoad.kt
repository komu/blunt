package komu.blunt.asm.opcodes

import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.Label
import komu.blunt.asm.Register
import komu.blunt.asm.VM
import komu.blunt.core.PatternPath
import komu.blunt.objects.CompoundProcedure
import komu.blunt.objects.TypeConstructorValue

sealed class OpLoad(private val target: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm[target] = load(vm)
    }

    abstract fun load(vm: VM): Any?
    abstract fun description(): String

    override fun modifies(register: Register) = register == target
    override fun toString() = "(load $target ${description()})"

    class Constant(target: Register, private val value: Any) : OpLoad(target) {

        init {
            require(target.isValidValue(value)) { "invalid value for register $target: $value" }
        }

        override fun load(vm: VM)= value
        override fun description() = "(constant $value)"
    }

    class Extracted(target: Register, private val source: Register, path: PatternPath) : OpLoad(target) {

        private val pathIndices = path.indices

        override fun load(vm: VM): Any? =
            pathIndices.fold(vm[source]) { obj, index ->
                (obj as TypeConstructorValue).items[index]
            }

        override fun description() = "(extract $source $pathIndices)"
    }

    class Tag(target: Register, private val source: Register, path: PatternPath) : OpLoad(target) {

        private val pathIndices = path.indices

        override fun load(vm: VM): Any? =
            pathIndices.fold(vm[source] as TypeConstructorValue) { obj, index ->
                obj.items[index] as TypeConstructorValue
            }.name

        override fun description() = "(tag $source $pathIndices)"
    }

    class LocalVariable(target: Register, private val variable: VariableReference) : OpLoad(target) {

        init {
            require(!variable.global)
        }

        override fun load(vm: VM) = vm.env[variable]
        override fun description() = "(variable ${variable.frame} ${variable.offset}) ; ${variable.name}"
    }

    class GlobalVariable(target: Register, private val variable: VariableReference) : OpLoad(target) {

        init {
            require(variable.global)
        }

        override fun load(vm: VM) = vm.globalEnvironment[variable]
        override fun description() = "(global-variable ${variable.offset}) ; ${variable.name}"
    }

    class Lambda(target: Register, private val label: Label) : OpLoad(target) {

        override fun load(vm: VM) =
            CompoundProcedure(label.address, vm.env)

        override fun description() = "(lambda $label ${Register.ENV})"
    }
}
