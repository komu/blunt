package komu.blunt.asm;

import com.google.common.base.Objects;
import komu.blunt.analyzer.VariableReference;
import komu.blunt.core.PatternPath;
import komu.blunt.eval.Environment;
import komu.blunt.objects.CompoundProcedure;
import komu.blunt.objects.Function;
import komu.blunt.objects.TypeConstructorValue;

import java.lang.reflect.Array;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.stdlib.BasicValues.booleanToConstructor;

public abstract class OpCode {
    OpCode() { }

    public abstract void execute(VM vm);

    public static final class JumpIfFalse extends OpCode {

        private final Register register;
        private final Label label;

        JumpIfFalse(Register register, Label label) {
            this.register = checkNotNull(register);
            this.label = checkNotNull(label);
        }

        @Override
        public void execute(VM vm) {
            Object value = vm.get(register);
            if (isFalse(value)) {
                vm.jump(label);
            }
        }

        private boolean isFalse(Object value) {
            return Boolean.FALSE.equals(value)
                || (value instanceof TypeConstructorValue && ((TypeConstructorValue) value).name.equals("False"));
        }

        @Override
        public String toString() {
            return String.format("(jump-if-false %s %s)", register, label);
        }
    }

    public static final class Jump extends OpCode {
        private final Label label;

        public Jump(Label label) {
            this.label = checkNotNull(label);
        }

        @Override
        public void execute(VM vm) {
            vm.jump(label);
        }

        @Override
        public String toString() {
            return String.format("(jump %s)", label);
        }
    }

    public static final class LoadConstant extends OpCode {
        private final Register register;
        private final Object value;

        public LoadConstant(Register register, Object value) {
            this.register = checkNotNull(register);
            this.value = value;
        }

        @Override
        public void execute(VM vm) {
            vm.set(register, value);
        }

        @Override
        public String toString() {
            return String.format("(load %s (constant %s))", register, value);
        }
    }

    public static final class LoadVariable extends OpCode {
        private final Register register;
        private final VariableReference variable;

        public LoadVariable(Register register, VariableReference variable) {
            this.register = checkNotNull(register);
            this.variable = checkNotNull(variable);
        }

        @Override
        public void execute(VM vm) {
            Environment env = (Environment) vm.get(Register.ENV);
            Object value = env.lookup(variable);
            vm.set(register, value);
        }

        @Override
        public String toString() {
            return String.format("(load %s (variable %d %d)) ; %s", register, variable.frame, variable.offset, variable.name);
        }
    }

    public static final class StoreVariable extends OpCode {
        private final VariableReference variable;
        private final Register register;

        public StoreVariable(VariableReference variable, Register register) {
            this.variable = checkNotNull(variable);
            this.register = checkNotNull(register);
        }

        @Override
        public void execute(VM vm) {
            Environment env = (Environment) vm.get(Register.ENV);
            Object value = vm.get(register);
            env.set(variable, value);
        }

        @Override
        public String toString() {
            return String.format("(store (variable %d %d) %s) ; %s", variable.frame, variable.offset, register, variable.name);
        }
    }

    public static final class LoadLambda extends OpCode {
        private final Register target;
        private final Label label;

        public LoadLambda(Register target, Label label) {
            this.target = checkNotNull(target);
            this.label = checkNotNull(label);
        }

        @Override
        public void execute(VM vm) {
            Environment environment = (Environment) vm.get(Register.ENV);
            CompoundProcedure procedure = new CompoundProcedure(label.getAddress(), environment);
            vm.set(target, procedure);
        }

        @Override
        public String toString() {
            return String.format("(load %s (lambda %s ENV))", target, label);
        }
    }
    
    public static class LoadConstructed extends OpCode {

        private final Register target;
        private final String name;
        private final int size;

        public LoadConstructed(Register target, String name, int size) {
            checkArgument(size >= 0);

            this.target = checkNotNull(target);
            this.name = checkNotNull(name);
            this.size = size;
        }

        @Override
        public void execute(VM vm) {
            Object[] array = new Object[size];
            for (int i = 0; i < size; i++)
                array[i] = vm.pop();
            vm.set(target, new TypeConstructorValue(name, array));
        }

        @Override
        public String toString() {
            return String.format("(load %s (%s %d))", target, name, size);
        }
    }

    public static class LoadTag extends OpCode {

        private final Register target;
        private final Register source;
        private final PatternPath path;

        public LoadTag(Register target, Register source, PatternPath path) {
            this.target = checkNotNull(target);
            this.source = checkNotNull(source);
            this.path = checkNotNull(path);
        }

        @Override
        public void execute(VM vm) {
            Object object = vm.get(source);
            for (int index : path.indices()) {
                object = ((TypeConstructorValue) object).items[index];
            }
            vm.set(target, ((TypeConstructorValue) object).name);
        }

        @Override
        public String toString() {
            return String.format("(load %s (tag %s %s))", target, source, path);
        }
    }

    public static class LoadExtracted extends OpCode {

        private Register target;
        private Register source;
        private PatternPath path;

        public LoadExtracted(Register target, Register source, PatternPath path) {
            this.target = checkNotNull(target);
            this.source = checkNotNull(source);
            this.path = checkNotNull(path);            
        }

        @Override
        public void execute(VM vm) {
            Object object = vm.get(source);
            for (int index : path.indices()) {
                object = ((TypeConstructorValue) object).items[index];
            }
            vm.set(target, object);
        }

        @Override
        public String toString() {
            return String.format("(load %s (extract %s %s))", target, source, path);
        }        
    }

    public static class LoadNewArray extends OpCode {
        private final Register target;
        private final int size;

        public LoadNewArray(Register target, int size) {
            this.target = checkNotNull(target);
            this.size = size;
        }

        @Override
        public void execute(VM vm) {
            Object[] value = new Object[size];
            vm.set(target, value);
        }

        @Override
        public String toString() {
            return String.format("(load %s (array %d))", target, size);
        }
    }

    public static class ArrayStore extends OpCode {
        private final Register arrayRegister;
        private final int index;
        private final Register valueRegister;

        public ArrayStore(Register arrayRegister, int index, Register valueRegister) {
            this.arrayRegister = checkNotNull(arrayRegister);
            this.index = index;
            this.valueRegister = checkNotNull(valueRegister);
        }

        @Override
        public void execute(VM vm) {
            Object array = vm.get(arrayRegister);
            Object value = vm.get(valueRegister);

            Array.set(array, index, value);
        }

        @Override
        public String toString() {
            return String.format("(array-store %s %d %s)", arrayRegister, index, valueRegister);
        }
    }

    public static class PushLabel extends OpCode {
        private final Label label;

        public PushLabel(Label label) {
            this.label = checkNotNull(label);
        }

        @Override
        public void execute(VM vm) {
            vm.push(label.getAddress());
        }

        @Override
        public String toString() {
            return String.format("(push (label %s))", label);
        }
    }

    public static class Apply extends OpCode {

        private final Register procedureRegister;
        private final Register argRegister;

        public Apply(Register procedureRegister, Register argRegister) {
            this.procedureRegister = procedureRegister;
            this.argRegister = argRegister;
        }

        @Override
        public void execute(VM vm) {
            Object procedure = vm.get(procedureRegister);
            Object arg = vm.get(argRegister);

            if (procedure instanceof Function) {
                executePrimitive(vm, (Function) procedure, arg);
            } else {
                executeCompound(vm, (CompoundProcedure) procedure, arg);
            }
        }

        private void executePrimitive(VM vm, Function procedure, Object arg) {
            Object value = procedure.apply(arg);
            vm.set(Register.VAL, value);
        }

        private void executeCompound(VM vm, CompoundProcedure procedure, Object arg) {
            Environment env = procedure.env.extend(arg);
            vm.save(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARG);
            vm.set(Register.ENV, env);
            vm.set(Register.PC, procedure.address);
        }

        @Override
        public String toString() {
            return String.format("(apply %s %s)", procedureRegister, argRegister);
        }
    }

    public static final class Return extends OpCode {
        @Override
        public String toString() {
            return "(return)";
        }

        @Override
        public void execute(VM vm) {
            vm.restore(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARG);
        }
    }

    public static class PushRegister extends OpCode {

        private final Register register;

        public PushRegister(Register register) {
            this.register = checkNotNull(register);
        }

        @Override
        public void execute(VM vm) {
            vm.push(vm.get(register));
        }

        @Override
        public String toString() {
            return String.format("(push %s)", register);
        }
    }

    public static class PopRegister extends OpCode {

        private final Register register;

        public PopRegister(Register register) {
            this.register = checkNotNull(register);
        }

        @Override
        public void execute(VM vm) {
            vm.set(register, vm.pop());
        }

        @Override
        public String toString() {
            return String.format("(pop %s)", register);
        }
    }

    public static class EqualConstant extends OpCode {

        private final Register target;
        private final Register source;
        private final Object value;

        public EqualConstant(Register target, Register source, Object value) {
            this.target = checkNotNull(target);
            this.source = checkNotNull(source);
            this.value = checkNotNull(value);
        }

        @Override
        public void execute(VM vm) {
            Object val = vm.get(source);

            boolean result = Objects.equal(val, value);
            vm.set(target, booleanToConstructor(result));
        }

        @Override
        public String toString() {
            return String.format("(load %s (= %s %s))", target, source, value);
        }
    }
    
    public static class CopyRegister extends OpCode {
        private final Register target;
        private final Register source;


        public CopyRegister(Register target, Register source) {
            this.target = checkNotNull(target);
            this.source = checkNotNull(source);
        }

        @Override
        public void execute(VM vm) {
            vm.set(target, vm.get(source));
        }

        @Override
        public String toString() {
            return String.format("(copy %s %s)", target, source);
        }
    }
}
