package komu.blunt.asm.opcodes

import komu.blunt.asm.Register
import komu.blunt.asm.VM
import komu.blunt.core.PatternPath
import komu.blunt.objects.TypeConstructorValue
import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.Label
import komu.blunt.objects.CompoundProcedure
import komu.blunt.eval.Environment
import komu.blunt.objects.Procedure

abstract class OpLoad(private val target: Register) : OpCode() {

    override fun execute(vm: VM) {
        vm.set(target, load(vm))
    }

    abstract fun load(vm: VM): Any?
    abstract fun description(): String

    override fun modifies(register: Register) = register == target

    override fun toString() = "(load $target ${description()})"
}

class OpLoadConstant(target: Register, private val value: Any) : OpLoad(target) {

    {
        if (!target.isValidValue(value))
            throw IllegalArgumentException("invalid value for register $target: $value")
    }

    override fun load(vm: VM)= value
    override fun description() = "(constant $value)"
}

class OpLoadExtracted(target: Register, private val source: Register, private val path: PatternPath) : OpLoad(target) {

    override fun load(vm: VM): Any? {
        var obj = vm.get(source)
        for (val index in path.indices())
            obj = (obj as TypeConstructorValue).items.sure()[index.sure()]
        return obj
    }

    override fun description() = "(extract $source $path)"
}

class OpLoadTag(target: Register, private val source: Register, private val path: PatternPath) : OpLoad(target) {

    override fun load(vm: VM): Any? {
        var obj = vm.get(source) as TypeConstructorValue
        for (val index in path.indices())
            obj = obj.items.sure()[index.sure()] as TypeConstructorValue
        return obj.name
    }

    override fun description() = "(tag $source $path)"
}

class OpLoadVariable(target: Register, private val variable: VariableReference) : OpLoad(target) {

    override fun load(vm: VM): Any? {
        val env = if (variable.isGlobal()) vm.globalEnvironment else vm.env
        return env.lookup(variable)
    }

    override fun description() = "(variable ${variable.frame} ${variable.offset} [${variable.name}])"
}

class OpLoadLambda(target: Register, private val label: Label) : OpLoad(target) {

    override fun load(vm: VM): CompoundProcedure {
        val env = vm.get(Register.ENV.sure()) as Environment
        return CompoundProcedure(label.address, env)
    }

    override fun description() = "(lambda $label ${Register.ENV})"
}
