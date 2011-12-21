package komu.blunt.eval;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.ast.ASTBuilder;
import komu.blunt.ast.ASTExpression;
import komu.blunt.core.CoreExpression;
import komu.blunt.objects.PrimitiveFunction;
import komu.blunt.reader.LispReader;
import komu.blunt.reader.Token;
import komu.blunt.stdlib.BasicFunctions;
import komu.blunt.stdlib.ConsList;
import komu.blunt.stdlib.LibraryFunction;
import komu.blunt.types.NativeTypeConversions;
import komu.blunt.types.Type;
import komu.blunt.types.TypeScheme;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isStatic;

public final class Evaluator {

    private final RootBindings rootBindings = new RootBindings();
    private final Analyzer analyzer = new Analyzer(rootBindings);
    private final Instructions instructions = new Instructions();
    private final VM vm = new VM(instructions, rootBindings.runtimeEnvironment);

    public Evaluator() {
        rootBindings.bind("true", Type.BOOLEAN, true);
        rootBindings.bind("false", Type.BOOLEAN, false);

        register(BasicFunctions.class, rootBindings);
        register(ConsList.class, rootBindings);
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

    public CoreExpression analyze(Object form) {
        CoreExpression exp = toCore(form);
        rootBindings.createTypeEnvironment().typeCheck(exp);
        return exp;
    }
    
    public void load(InputStream in) throws IOException {
        try {
            LispReader reader = new LispReader(in);
            Object form;
            while ((form = reader.readForm()) != Token.EOF) {
                evaluate(form);
            }
        } finally {
            in.close();
        }
    }

    public Object evaluate(Object form) {
        return evaluateWithType(form).result;
    }

    public ResultWithType evaluateWithType(Object form) {
        CoreExpression expression = toCore(form);
        Type type = rootBindings.createTypeEnvironment().typeCheck(expression);

        int pos = instructions.pos();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        vm.set(Register.PC, pos);
        Object result = vm.run();

        return new ResultWithType(result, type);
    }

    private CoreExpression toCore(Object form) {
        ASTExpression ast = new ASTBuilder().parse(form);
        return analyzer.analyze(ast, rootBindings.staticEnvironment);
    }

    public void dump() {
        instructions.dump();
    }
}
