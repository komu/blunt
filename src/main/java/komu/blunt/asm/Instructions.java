package komu.blunt.asm;

import komu.blunt.asm.opcodes.*;

import java.util.*;

public final class Instructions {

    private final List<OpCode> instructions = new ArrayList<>();
    private final Map<Integer,Set<Label>> labelMap = new HashMap<>();

    public void append(Instructions rhs) {
        int relocationOffset = instructions.size();
        
        instructions.addAll(rhs.instructions);
        
        for (Set<Label> labels : rhs.labelMap.values()) {
            for (Label label : labels) {
                label.relocateBy(relocationOffset);
                getLabels(label.getAddress()).add(label);
            }
        }
    }

    public boolean modifies(Register register) {
        for (OpCode op : instructions)
            if (op.modifies(register))
                return true;

        return false;
    }

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
            labels = new HashSet<>();
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

    public void add(OpCode op) {
        instructions.add(op);
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
