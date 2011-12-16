package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.asm.VM;
import fi.evident.dojolisp.ast.Expression;
import fi.evident.dojolisp.objects.PrimitiveFunction;
import fi.evident.dojolisp.stdlib.BasicFunctions;
import fi.evident.dojolisp.stdlib.LibraryFunction;
import fi.evident.dojolisp.types.NativeTypeConversions;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeScheme;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Evaluator {

    private final Analyzer analyzer = new Analyzer();
    private final Environments environments;

    public Evaluator() {
        StaticBindings bindings = new StaticBindings();
        
        bindings.bind("true", Type.BOOLEAN, true);
        bindings.bind("false", Type.BOOLEAN, false);

        register(BasicFunctions.class, bindings);

        this.environments = bindings.createEnvironments();
    }
    
    private static void register(Class<?> cl, StaticBindings bindings) {
        for (Method m : cl.getMethods()) {
            LibraryFunction func = m.getAnnotation(LibraryFunction.class);
            if (func != null && Modifier.isStatic(m.getModifiers())) {
                String name = func.value();
                TypeScheme type = NativeTypeConversions.createFunctionType(m);
                bindings.bind(name, type, new PrimitiveFunction(name, m));
            }
        }
    }

    public Expression analyze(Object form) {
        Expression exp = analyzer.analyze(form, environments.staticEnvironment);
        environments.typeEnvironment.typeCheck(exp);
        return exp;
    }

    public Object evaluate(Object form) {
        return evaluateWithType(form).result;
    }
    
    public ResultWithType evaluateWithType(Object form) {
        Expression expression = analyzer.analyze(form, environments.staticEnvironment);
        Type type = environments.typeEnvironment.typeCheck(expression);

        Instructions instructions = new Instructions();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        VM vm = new VM(instructions, environments.runtimeEnvironment);
        Object result = vm.run();

        return new ResultWithType(result, type);
    }
}
