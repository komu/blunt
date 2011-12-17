package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.asm.VM;
import fi.evident.dojolisp.ast.Expression;
import fi.evident.dojolisp.objects.PrimitiveFunction;
import fi.evident.dojolisp.stdlib.BasicFunctions;
import fi.evident.dojolisp.stdlib.ConsList;
import fi.evident.dojolisp.stdlib.LibraryFunction;
import fi.evident.dojolisp.types.NativeTypeConversions;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeScheme;

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

    public Expression analyze(Object form) {
        Expression exp = analyzer.analyze(form, rootBindings.staticEnvironment);
        rootBindings.typeEnvironment.typeCheck(exp);
        return exp;
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
}
