package komu.blunt.eval;

import static java.lang.reflect.Modifier.isStatic;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.io.Resources;

import komu.blunt.analyzer.AnalyzingVisitor;
import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.ast.ASTDefine;
import komu.blunt.ast.ASTExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.PrimitiveFunction;
import komu.blunt.parser.Parser;
import komu.blunt.stdlib.BasicFunctions;
import komu.blunt.stdlib.BasicValues;
import komu.blunt.stdlib.ConsList;
import komu.blunt.stdlib.LibraryFunction;
import komu.blunt.stdlib.LibraryValue;
import komu.blunt.stdlib.Maybe;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.NativeTypeConversions;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.checker.TypeChecker;

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

        for (ASTDefine define : parser.parseDefinitions())
            define(define);
    }

    public void define(ASTDefine define) {
        Scheme type = TypeChecker.typeCheck(define, classEnv, rootBindings.createAssumptions());
        
        rootBindings.defineVariableType(define.name, type);
        CoreExpression expression = define.analyze(rootBindings.staticEnvironment);

        run(expression);
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
        return TypeChecker.typeCheck(exp, classEnv, rootBindings.createAssumptions());
    }

    private CoreExpression toCore(ASTExpression exp) {
        AnalyzingVisitor analyzer = new AnalyzingVisitor();

        return analyzer.analyze(exp, rootBindings.staticEnvironment);
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
