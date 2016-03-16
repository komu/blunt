package komu.blunt.eval

import komu.blunt.analyzer.StaticEnvironment
import komu.blunt.analyzer.analyze
import komu.blunt.asm.*
import komu.blunt.ast.ASTDataDefinition
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTValueDefinition
import komu.blunt.core.CoreDefineExpression
import komu.blunt.core.CoreExpression
import komu.blunt.parser.Parser
import komu.blunt.stdlib.BasicFunctions
import komu.blunt.types.*
import komu.blunt.types.checker.TypeChecker
import komu.blunt.utils.readResourceAsString

class Evaluator() {

    private val rootBindings = RootBindings()
    private val instructions = Instructions()
    private val classEnv = ClassEnv()
    var steps = 0.toLong();

    init {
        BasicFunctions.register(rootBindings)
        //throw UnsupportedOperationException("registering basic functions")
        //NativeFunctionRegisterer(rootBindings).register(Class.forName("komu.blunt.stdlib.BasicFunctions"))

        for (constructor in rootBindings.dataTypes.declaredConstructors)
            createConstructorFunction(constructor)
    }

    fun analyze(expr: ASTExpression): CoreExpression {
        typeCheck(expr)
        return toCore(expr, rootBindings.staticEnvironment.extend())
    }

    fun load(source: String) {
        val parser = Parser(source)

        for (define in parser.parseDefinitions())
            when (define) {
                is ASTValueDefinition -> processDefinition(define)
                is ASTDataDefinition  -> register(define)
                else -> throw Exception("invalid definition $define")
            }
    }

    private fun processDefinition(definition: ASTValueDefinition) {
        try {
            val typ = TypeChecker.typeCheck(definition, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions())

            rootBindings.defineVariableType(definition.name, typ)
            val v = rootBindings.staticEnvironment.define(definition.name)

            val exp = analyze(definition.value, rootBindings.dataTypes, rootBindings.staticEnvironment)

            run(CoreDefineExpression(v, exp), rootBindings.runtimeEnvironment)

        } catch (e: TypeCheckException) {
            println("failure in definition: $definition")
            throw e
        }
    }

    private fun register(definition: ASTDataDefinition) {
        rootBindings.dataTypes.register(definition)

        for (ctor in definition.constructors)
            createConstructorFunction(ctor)

        for (className in definition.derivedClasses)
            classEnv.addInstance(isIn(className, definition.typ))
    }

    private fun createConstructorFunction(ctor: ConstructorDefinition) {
        if (ctor.arity != 0)
            rootBindings.bind(ctor.name, ctor.scheme, ConstructorArgumentCollector.createConstructor(ctor));
    }

    private fun run(expression: CoreExpression, env: Environment): Any? {
        val pos = instructions.count

        instructions.append(expression.simplify().assemble(Assembler(), Register.VAL, Linkage.NEXT))

        val vm = VM(instructions, env, rootBindings.runtimeEnvironment)
        vm.pc = pos
        val result = vm.run()
        steps += vm.steps
        return result
    }

    public fun evaluate(exp: ASTExpression): Any? =
        evaluateWithType(exp).first

    public fun evaluateWithType(exp: ASTExpression): Pair<Any?, Qualified<Type>> {
        val env = rootBindings.staticEnvironment.extend()
        val typ = typeCheck(exp)
        val expression = toCore(exp, env);

        val result = run(expression, rootBindings.runtimeEnvironment.extend(env.size))

        return Pair(result, typ)
    }

    private fun typeCheck(exp: ASTExpression) =
        TypeChecker.typeCheck(exp, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions())

    private fun toCore(exp: ASTExpression, env: StaticEnvironment) =
        analyze(exp, rootBindings.dataTypes, env)

    fun dump() {
        instructions.dump()
    }

    fun loadResource(path: String) {
        load(readResource(path))
    }

    private fun readResource(path: String): String =
        javaClass.getClassLoader()!!.readResourceAsString(path) ?:
            throw Exception("could not find resource: $path")
}
