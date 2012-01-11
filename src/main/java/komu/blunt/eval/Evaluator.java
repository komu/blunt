package komu.blunt.eval;

import com.google.common.io.Resources;
import komu.blunt.analyzer.Analyzer;
import komu.blunt.analyzer.StaticEnvironment;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.*;
import komu.blunt.ast.*;
import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.parser.Parser;
import komu.blunt.stdlib.BasicFunctions;
import komu.blunt.types.*;
import komu.blunt.types.checker.TypeChecker;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import static komu.blunt.eval.ConstructorArgumentCollector.createConstructor;
import static komu.blunt.types.Predicate.isIn;

public final class Evaluator {

    private final RootBindings rootBindings = new RootBindings();
    private final Instructions instructions = new Instructions();
    private final ClassEnv classEnv = new ClassEnv();
    private long steps = 0;

    public Evaluator() {
        new NativeFunctionRegisterer(rootBindings).register(BasicFunctions.class);

        for (ConstructorDefinition constructor : rootBindings.dataTypes.getDeclaredConstructors())
            createConstructorFunction(constructor);
    }

    public CoreExpression analyze(ASTExpression expr) {
        typeCheck(expr);
        CoreExpression exp = toCore(expr, rootBindings.staticEnvironment.extend());
        return exp;
    }
    
    public void load(String source) {
        Parser parser = new Parser(source);

        MyDefinitionVisitor visitor = new MyDefinitionVisitor();
        for (ASTDefinition define : parser.parseDefinitions())
            define.accept(visitor, null);
    }

    private final class MyDefinitionVisitor implements ASTDefinitionVisitor<Void,Void> {
        @Override
        public Void visit(ASTValueDefinition definition, Void ctx) {
            Scheme type = TypeChecker.typeCheck(definition, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions());
            
            rootBindings.defineVariableType(definition.name, type);
            VariableReference var = rootBindings.staticEnvironment.define(definition.name);

            CoreExpression exp = Analyzer.analyze(definition.value, rootBindings.dataTypes, rootBindings.staticEnvironment);

            run(new CoreDefineExpression(exp, var), rootBindings.runtimeEnvironment);
            return null;
        }

        @Override
        public Void visit(ASTDataDefinition definition, Void ctx) {
            register(definition);

            return null;
        }
    }

    private void register(ASTDataDefinition definition) {
        rootBindings.dataTypes.register(definition);

        for (ConstructorDefinition ctor : definition.constructors)
            createConstructorFunction(ctor);
        
        for (String className : definition.derivedClasses) {
            classEnv.addInstance(isIn(className, definition.type));
        }
    }

    private void createConstructorFunction(ConstructorDefinition ctor) {
        if (ctor.arity != 0)
            rootBindings.bind(ctor.name, ctor.scheme, createConstructor(ctor));
    }

    private Object run(CoreExpression expression, Environment env) {
        int pos = instructions.pos();

        instructions.append(expression.simplify().assemble(new Assembler(), Register.VAL, Linkage.NEXT));

        VM vm = new VM(instructions, env, rootBindings.runtimeEnvironment);
        vm.set(Register.PC, pos);
        Object result = vm.run();
        steps += vm.steps;
        return result;
    }

    public Object evaluate(ASTExpression exp) {
        return evaluateWithType(exp).result;
    }

    public ResultWithType evaluateWithType(ASTExpression exp) {
        StaticEnvironment env = rootBindings.staticEnvironment.extend();
        Qualified<Type> type = typeCheck(exp);
        CoreExpression expression = toCore(exp, env);

        Object result = run(expression, rootBindings.runtimeEnvironment.extend(env.size()));

        return new ResultWithType(result, type);
    }

    private Qualified<Type> typeCheck(ASTExpression exp) {
        return TypeChecker.typeCheck(exp, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions());
    }

    private CoreExpression toCore(ASTExpression exp, StaticEnvironment env) {
        return Analyzer.analyze(exp, rootBindings.dataTypes, env);
    }

    public long getSteps() {
        return steps;
    }

    public void dump() {
        instructions.dump();
    }

    public void loadResource(String path) throws IOException {
        load(readResource(path));
    }

    private String readResource(String path) throws IOException {
        URL resource = getClass().getClassLoader().getResource(path);
        return Resources.toString(resource, Charset.forName("UTF-8"));
    }
}
