package komu.blunt.eval

import com.google.common.io.Resources
import komu.blunt.analyzer.Analyzer
import komu.blunt.analyzer.StaticEnvironment
import komu.blunt.analyzer.VariableReference
import komu.blunt.asm.*
import komu.blunt.ast.*
import komu.blunt.core.CoreDefineExpression
import komu.blunt.core.CoreExpression
import komu.blunt.stdlib.BasicFunctions
import komu.blunt.types.*
import komu.blunt.types.checker.TypeChecker

import java.io.IOException
import java.net.URL
import java.nio.charset.Charset

import komu.blunt.types.isIn
import komu.blunt.parser.Parser

class Evaluator() {

    private val rootBindings = RootBindings()
    private val instructions = Instructions()
    private val classEnv = ClassEnv()
    private var steps = 0.toLong();

    {
        BasicFunctions.register(rootBindings)
        //throw UnsupportedOperationException("registering basic functions")
        //NativeFunctionRegisterer(rootBindings).register(Class.forName("komu.blunt.stdlib.BasicFunctions"))

        for (val constructor in rootBindings.dataTypes.getDeclaredConstructors())
            createConstructorFunction(constructor)
    }

    fun analyze(expr: ASTExpression): CoreExpression {
        typeCheck(expr)
        return toCore(expr, rootBindings.staticEnvironment.extend())
    }

    fun load(source: String) {
        val parser = Parser(source)

        for (val define in parser.parseDefinitions())
            when (define) {
                is ASTValueDefinition -> processDefinition(define)
                is ASTDataDefinition  -> register(define)
                else -> throw Exception("invalid definition $define")
            }
    }

    private fun processDefinition(definition: ASTValueDefinition) {
        val typ = TypeChecker.typeCheck(definition, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions())

        rootBindings.defineVariableType(definition.name, typ)
        val v = rootBindings.staticEnvironment.define(definition.name)

        val exp = Analyzer.analyze(definition.value, rootBindings.dataTypes, rootBindings.staticEnvironment)

        run(CoreDefineExpression(v, exp), rootBindings.runtimeEnvironment);
    }

    private fun register(definition: ASTDataDefinition) {
        rootBindings.dataTypes.register(definition)

        for (val ctor in definition.constructors)
            createConstructorFunction(ctor)

        for (val className in definition.derivedClasses)
            classEnv.addInstance(isIn(className, definition.typ))
    }

    private fun createConstructorFunction(ctor: ConstructorDefinition) {
        if (ctor.arity != 0)
            rootBindings.bind(ctor.name, ctor.scheme, ConstructorArgumentCollector.createConstructor(ctor));
    }

    private fun run(expression: CoreExpression, env: Environment): Any? {
        val pos = instructions.pos();

        instructions.append(expression.simplify().assemble(Assembler(), Register.VAL.sure(), Linkage.NEXT))

        val vm = VM(instructions, env, rootBindings.runtimeEnvironment)
        vm.set(Register.PC.sure(), pos)
        val result = vm.run()
        steps += vm.steps
        return result;
    }

    public fun evaluate(exp: ASTExpression): Any? =
        evaluateWithType(exp)._1


    public fun evaluateWithType(exp: ASTExpression): #(Any?,Qualified<Type>) {
        val env = rootBindings.staticEnvironment.extend()
        val typ = typeCheck(exp)
        val expression = toCore(exp, env);

        val result = run(expression, rootBindings.runtimeEnvironment.extend(env.size))

        return #(result, typ)
    }

    private fun typeCheck(exp: ASTExpression): Qualified<Type> {
        return TypeChecker.typeCheck(exp, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions())
    }

    private fun toCore(exp: ASTExpression, env: StaticEnvironment): CoreExpression =
        Analyzer.analyze(exp, rootBindings.dataTypes, env)

    fun getSteps() = steps

    fun dump() {
        instructions.dump()
    }

    fun loadResource(path: String) {
        load(readResource(path))
    }

    private fun readResource(path: String): String {
        val resource = getMyClass().getClassLoader()?.getResource(path)
        return Resources.toString(resource, Charset.forName("UTF-8")).sure()
    }

    private fun getMyClass() = Class.forName("komu.blunt.eval.Evaluator").sure() // TODO
}

