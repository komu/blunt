package fi.evident.dojolisp.asm;

import fi.evident.dojolisp.eval.Environment;
import fi.evident.dojolisp.eval.VariableReference;
import fi.evident.dojolisp.objects.CompoundProcedure;
import fi.evident.dojolisp.objects.Function;

import java.lang.reflect.Array;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public abstract class OpCode {
    OpCode() { }

    public abstract void execute(VM vm);

    public static final class JumpIfFalse extends OpCode {

        private final Register register;
        private final Label label;

        JumpIfFalse(Register register, Label label) {
            this.register = requireNonNull(register);
            this.label = requireNonNull(label);
        }

        @Override
        public void execute(VM vm) {
            Object value = vm.get(register);
            if (Boolean.FALSE.equals(value)) {
                vm.jump(label);
            }
        }

        @Override
        public String toString() {
            return String.format("(jump-if-false %s %s)", register, label);
        }
    }

    public static final class Jump extends OpCode {
        private final Label label;

        public Jump(Label label) {
            this.label = requireNonNull(label);
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
            this.register = requireNonNull(register);
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
            this.register = requireNonNull(register);
            this.variable = requireNonNull(variable);
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

    public static final class LoadLambda extends OpCode {
        private final Register target;
        private final Label label;

        public LoadLambda(Register target, Label label) {
            this.target = requireNonNull(target);
            this.label = requireNonNull(label);
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
        
    public static class LoadNewArray extends OpCode {
        private final Register target;
        private final int size;

        public LoadNewArray(Register target, int size) {
            this.target = requireNonNull(target);
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
            this.arrayRegister = requireNonNull(arrayRegister);
            this.index = index;
            this.valueRegister = requireNonNull(valueRegister);
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
            this.label = requireNonNull(label);
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
        private final Register argvRegister;

        public Apply(Register procedureRegister, Register argvRegister) {
            this.procedureRegister = procedureRegister;
            this.argvRegister = argvRegister;
        }

        @Override
        public void execute(VM vm) {
            Object procedure = vm.get(procedureRegister);
            Object[] args = (Object[]) vm.get(argvRegister);

            if (procedure instanceof Function) {
                executePrimitive(vm, (Function) procedure, args);
            } else {
                executeCompound(vm, (CompoundProcedure) procedure, args);
            }
        }

        private void executePrimitive(VM vm, Function procedure, Object[] args) {
            Object value = procedure.apply(args);
            vm.set(Register.VAL, value);
        }

        private void executeCompound(VM vm, CompoundProcedure procedure, Object[] args) {
            Environment env = procedure.env.extend(args);
            vm.save(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARGV);
            vm.set(Register.ENV, env);
            vm.set(Register.PC, procedure.address);
        }

        @Override
        public String toString() {
            return String.format("(apply %s %s)", procedureRegister, argvRegister);
        }
    }

    public static final class Return extends OpCode {
        @Override
        public String toString() {
            return "(return)";
        }

        @Override
        public void execute(VM vm) {
            vm.restore(Register.ENV, Register.PC, Register.PROCEDURE, Register.ARGV);
        }
    }

    public static class PushRegister extends OpCode {

        private final Register register;

        public PushRegister(Register register) {
            this.register = requireNonNull(register);
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
            this.register = requireNonNull(register);
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

    public static class CopyRegister extends OpCode {
        private final Register target;
        private final Register source;


        public CopyRegister(Register target, Register source) {
            this.target = requireNonNull(target);
            this.source = requireNonNull(source);
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

