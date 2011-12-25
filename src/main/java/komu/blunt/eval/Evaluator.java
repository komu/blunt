package komu.blunt.eval;

import komu.blunt.Main;
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
import komu.blunt.stdlib.ConsList;
import komu.blunt.stdlib.LibraryFunction;
import komu.blunt.stdlib.Maybe;
import komu.blunt.types.NativeTypeConversions;
import komu.blunt.types.Type;
import komu.blunt.types.TypeScheme;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isStatic;

public final class Evaluator {

    private final RootBindings rootBindings = new RootBindings();
    private final Instructions instructions = new Instructions();
    private final VM vm = new VM(instructions, rootBindings.runtimeEnvironment);

    public Evaluator() {
        rootBindings.bind("True", Type.BOOLEAN, true);
        rootBindings.bind("False", Type.BOOLEAN, false);

        register(BasicFunctions.class, rootBindings);
        register(ConsList.class, rootBindings);
        register(Maybe.class, rootBindings);
    }
    
    private static void register(Class<?> cl, RootBindings bindings) {
        for (Method m : cl.getDeclaredMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null) {
                String name = func.value();
                TypeScheme type = NativeTypeConversions.createFunctionType(m);

                boolean isStatic = isStatic(m.getModifiers());
                bindings.bind(name, type, new PrimitiveFunction(name, m, isStatic));
            }
        }
    }

    public CoreExpression analyze(ASTExpression expr) {
        rootBindings.createTypeEnvironment().typeCheck(expr);
        CoreExpression exp = toCore(expr);
        return exp;
    }
    
    public void load(InputStream in) throws IOException {
        try {
            Parser parser = new Parser(in);

            for (ASTDefine define : parser.parseDefinitions())
                define(define);

        } finally {
            in.close();
        }
    }

    public void define(ASTDefine define) {
        define.typeCheck(rootBindings);
        CoreExpression expression = define.analyze(rootBindings.staticEnvironment, rootBindings);

        run(expression);
    }

    private Object run(CoreExpression expression) {
        int pos = instructions.pos();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        vm.set(Register.PC, pos);
        return vm.run();
    }

    public Object evaluate(ASTExpression exp) {
        return evaluateWithType(exp).result;
    }

    public ResultWithType evaluateWithType(ASTExpression exp) {
        Type type = rootBindings.createTypeEnvironment().typeCheck(exp);
        CoreExpression expression = toCore(exp);

        Object result = run(expression);

        return new ResultWithType(result, type);
    }

    private CoreExpression toCore(ASTExpression exp) {
        return exp.analyze(rootBindings.staticEnvironment, rootBindings);
    }

    public void dump() {
        instructions.dump();
    }

    public void loadResource(String path) throws IOException {
        load(openResource(path));
    }

    private static InputStream openResource(String path) throws FileNotFoundException {
        ClassLoader loader = Main.class.getClassLoader();

        InputStream in = loader.getResourceAsStream(path);
        if (in != null)
            return in;
        else
            throw new FileNotFoundException("file not found: " + path);
    }
}
