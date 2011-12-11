package fi.evident.dojolisp.eval;

import fi.evident.dojolisp.asm.Instructions;
import fi.evident.dojolisp.asm.Linkage;
import fi.evident.dojolisp.asm.Register;
import fi.evident.dojolisp.asm.VM;
import fi.evident.dojolisp.ast.Expression;
import fi.evident.dojolisp.objects.PrimitiveFunction;
import fi.evident.dojolisp.stdlib.BasicFunctions;
import fi.evident.dojolisp.stdlib.LibraryFunction;
import fi.evident.dojolisp.types.FunctionType;
import fi.evident.dojolisp.types.Type;
import fi.evident.dojolisp.types.TypeEnvironment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

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
                Type type = createFunctionType(m);
                bindings.bind(name, type, new PrimitiveFunction(name, m));
            }
        }
    }

    private static Type createFunctionType(Method m) {
        // TODO: support parsing signatures from 'func'

        List<Type> argumentTypes = new ArrayList<Type>(m.getParameterTypes().length);
        for (Class<?> type : m.getParameterTypes())
            argumentTypes.add(Type.fromClass(type));
        
        Type returnType = Type.fromClass(m.getReturnType());
        
        if (m.isVarArgs()) {
            int last = argumentTypes.size()-1;
            argumentTypes.set(last, Type.fromClass(m.getParameterTypes()[last].getComponentType()));

            return new FunctionType(argumentTypes, returnType, true);
        } else {
            return new FunctionType(argumentTypes, returnType, false);
        }
    }

    public Expression analyze(Object form) {
        Expression exp = analyzer.analyze(form, environments.staticEnvironment);
        exp.typeCheck(new TypeEnvironment());
        return exp;
    }

    public Object evaluate(Object form) {
        return evaluateWithType(form).result;
    }
    
    public ResultWithType evaluateWithType(Object form) {
        Expression expression = analyzer.analyze(form, environments.staticEnvironment);
        Type type = expression.typeCheck(new TypeEnvironment());

        Instructions instructions = new Instructions();
        expression.assemble(instructions, Register.VAL, Linkage.NEXT);

        VM vm = new VM(instructions, environments.runtimeEnvironment);
        Object result = vm.run();

        return new ResultWithType(result, type);
    }
}
