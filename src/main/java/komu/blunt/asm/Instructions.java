package komu.blunt.asm;

import komu.blunt.analyzer.VariableReference;
import komu.blunt.asm.opcodes.*;
import komu.blunt.core.PatternPath;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Instructions {
    
    private final List<OpCode> instructions = new ArrayList<OpCode>();
    private final Map<Integer,Set<Label>> labelMap = new HashMap<Integer,Set<Label>>();
    private int labelCounter = 0;

    public void jumpIfFalse(Register register, Label label) {
        instructions.add(new OpJumpIfFalse(register, label));
    }
    
    public void label(Label label) {
        label.setAddress(instructions.size());
        getLabels(label.getAddress()).add(label);
    }

    private Set<Label> getLabels(int address) {
        Set<Label> labels = labelMap.get(address);
        if (labels == null) {
            labels = new HashSet<Label>();
            labelMap.put(address, labels);
        }
        return labels;
    }

    public void dump() {
        int address = 0;
        for (Object instruction : instructions) {
            for (Label label : getLabels(address++))
                System.out.println(label + ":");

            System.out.println("    " + instruction);
        }
    }

    public Label newLabel(String prefix) {
        return new Label(checkNotNull(prefix) + "-" + labelCounter++);
    }

    public void finishWithLinkage(Linkage linkage) {
        if (linkage == Linkage.NEXT) {
            // nada
        } else if (linkage == Linkage.RETURN) {
            instructions.add(new OpReturn());
        } else {
            jump(linkage.label);
        }
    }

    public void loadConstant(Register target, Object value) {
        instructions.add(new OpLoadConstant(target, value));
    }

    public void loadVariable(Register target, VariableReference variable) {
        instructions.add(new OpLoadVariable(target, variable));
    }
    
    public void storeVariable(VariableReference var, Register val) {
        instructions.add(new OpStoreVariable(var, val));
    }
    
    public void loadLambda(Register target, Label label) {
        instructions.add(new OpLoadLambda(target, label));
    }

    public void createEnvironment(int envSize) {
        instructions.add(new OpCreateEnvironment(envSize));
    }

    public void loadExtracted(Register target, Register source, PatternPath path) {
        instructions.add(new OpLoadExtracted(target, source, path));
    }

    public void loadTag(Register target, Register source, PatternPath path) {
        instructions.add(new OpLoadTag(target, source, path));
    }

    public void jump(Label label) {
        instructions.add(new OpJump(label));
    }

    public void pushRegister(Register register) {
        instructions.add(new OpPushRegister(register));
    }

    public void popRegister(Register register) {
        instructions.add(new OpPopRegister(register));
    }

    public void apply() {
        instructions.add(OpApply.INSTANCE);
    }
    
    public void copy(Register target, Register source) {
        instructions.add(new OpCopyRegister(target, source));
    }
    
    public void equalConstant(Register target, Register source, Object value) {
        instructions.add(new OpEqualConstant(target, source, value));
    }
    
    public void loadConstructed(Register target, int index, String name, int size) {
        instructions.add(new OpLoadConstructed(target, index, name, size));
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
