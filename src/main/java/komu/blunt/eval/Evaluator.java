package komu.blunt.eval;

import static java.lang.reflect.Modifier.isStatic;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import komu.blunt.asm.Instructions;
import komu.blunt.asm.Linkage;
import komu.blunt.asm.Register;
import komu.blunt.asm.VM;
import komu.blunt.ast.Expression;
import komu.blunt.objects.PrimitiveFunction;
import komu.blunt.reader.LispReader;
import komu.blunt.reader.Token;
import komu.blunt.stdlib.BasicFunctions;
import komu.blunt.stdlib.ConsList;
import komu.blunt.stdlib.LibraryFunction;
import komu.blunt.types.NativeTypeConversions;
import komu.blunt.types.Type;
import komu.blunt.types.TypeScheme;

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

    public Expression analyze(Object form) {
        Expression exp = analyzer.analyze(form, rootBindings.staticEnvironment);
        rootBindings.typeEnvironment.typeCheck(exp);
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
        Expression expression = analyzer.analyze(form, rootBindings.staticEnvironment);
        Type type = rootBindings.typeEnvironment.typeCheck(expression);

        int pos = instructions.pos();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        vm.set(Register.PC, pos);
        Object result = vm.run();

        return new ResultWithType(result, type);
    }

    public void dump() {
        instructions.dump();
    }
}
