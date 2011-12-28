package komu.blunt.eval;

import com.google.common.io.Resources;
import komu.blunt.analyzer.Analyzer;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.ast.*;
import komu.blunt.core.CoreDefineExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.PrimitiveFunction;
import komu.blunt.parser.Parser;
import komu.blunt.stdlib.*;
import komu.blunt.types.*;
import komu.blunt.types.checker.TypeChecker;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;

import static java.lang.reflect.Modifier.isStatic;

public final class Evaluator {

    private final RootBindings rootBindings = new RootBindings();
    private final Instructions instructions = new Instructions();
    private final ClassEnv classEnv = new ClassEnv();

    public Evaluator() {
        register(BasicValues.class, rootBindings);
        register(BasicFunctions.class, rootBindings);
        register(ConsList.class, rootBindings);
        register(Maybe.class, rootBindings);
    }
    
    private static void register(Class<?> cl, RootBindings bindings) {
        for (Method m : cl.getDeclaredMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null) {
                String name = func.value();
                Scheme type = NativeTypeConversions.createFunctionType(m);

                boolean isStatic = isStatic(m.getModifiers());
                bindings.bind(name, type, new PrimitiveFunction(name, m, isStatic));
            }
        }
        
        for (Field f : cl.getDeclaredFields()) {
            LibraryValue value = f.getAnnotation(LibraryValue.class);
            if (value != null) {
                Scheme scheme = Type.fromClass(f.getType()).toScheme();

                try {
                    bindings.bind(value.value(), scheme, f.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public CoreExpression analyze(ASTExpression expr) {
        typeCheck(expr);
        CoreExpression exp = toCore(expr);
        return exp;
    }
    
    public void load(String source) throws IOException {
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

            run(new CoreDefineExpression(exp, var));
            return null;
        }

        @Override
        public Void visit(ASTDataDefinition definition, Void ctx) {
            System.out.println(definition);
            rootBindings.dataTypes.register(definition);
            return null;
        }
    }

    private Object run(CoreExpression expression) {
        int pos = instructions.pos();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        VM vm = new VM(instructions, rootBindings.runtimeEnvironment);
        vm.set(Register.PC, pos);
        return vm.run();
    }

    public Object evaluate(ASTExpression exp) {
        return evaluateWithType(exp).result;
    }

    public ResultWithType evaluateWithType(ASTExpression exp) {
        Qualified<Type> type = typeCheck(exp);
        CoreExpression expression = toCore(exp);

        Object result = run(expression);

        return new ResultWithType(result, type);
    }

    private Qualified<Type> typeCheck(ASTExpression exp) {
        return TypeChecker.typeCheck(exp, classEnv, rootBindings.dataTypes, rootBindings.createAssumptions());
    }

    private CoreExpression toCore(ASTExpression exp) {
        return Analyzer.analyze(exp, rootBindings.dataTypes, rootBindings.staticEnvironment);
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
