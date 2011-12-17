package fi.evident.dojolisp.asm;

import fi.evident.dojolisp.eval.VariableReference;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Instructions {
    
    private final List<OpCode> instructions = new ArrayList<OpCode>();
    private int labelCounter = 0;

    public void jumpIfFalse(Register register, Label label) {
        instructions.add(new OpCode.JumpIfFalse(register, label));
    }
    
    public void label(Label label) {
        label.setAddress(instructions.size());
    }

    @SuppressWarnings("unused")
    public void dump() {
        for (Object instruction : instructions) {
            if (instruction instanceof Label) {
                System.out.println(instruction + ":");
            } else {
                System.out.println("    " + instruction);
            }
        }
    }

    public Label newLabel(String prefix) {
        return new Label(requireNonNull(prefix) + "-" + labelCounter++);
    }

    public void finishWithLinkage(Linkage linkage) {
        if (linkage == Linkage.NEXT) {
            // nada
        } else if (linkage == Linkage.RETURN) {
            instructions.add(new OpCode.Return());
        } else {
            jump(linkage.label);
        }
    }

    public void loadConstant(Register target, Object value) {
        instructions.add(new OpCode.LoadConstant(target, value));
    }

    public void loadVariable(Register target, VariableReference variable) {
        instructions.add(new OpCode.LoadVariable(target, variable));
    }
    
    public void storeVariable(VariableReference var, Register val) {
        instructions.add(new OpCode.StoreVariable(var, val));
    }
    
    public void loadLambda(Register target, Label label) {
        instructions.add(new OpCode.LoadLambda(target, label));
    }

    public void loadNewArray(Register target, int size) {
        instructions.add(new OpCode.LoadNewArray(target, size));
    }

    public void arrayStore(Register array, int offset, Register val) {
        instructions.add(new OpCode.ArrayStore(array, offset, val));
    }
    
    public void jump(Label label) {
        instructions.add(new OpCode.Jump(label));
    }

    public void pushLabel(Label label) {
        instructions.add(new OpCode.PushLabel(label));
    }

    public void pushRegister(Register register) {
        instructions.add(new OpCode.PushRegister(register));
    }

    public void popRegister(Register register) {
        instructions.add(new OpCode.PopRegister(register));
    }

    public void apply(Register procedure, Register argv) {
        instructions.add(new OpCode.Apply(procedure, argv));
    }
    
    public void copy(Register target, Register source) {
        instructions.add(new OpCode.CopyRegister(target, source));
    }

    public int count() {
        return instructions.size();
    }
    
    public OpCode get(int pc) {
        return instructions.get(pc);
    }

    public int pos() {
        return instructions.size();
    }
}
