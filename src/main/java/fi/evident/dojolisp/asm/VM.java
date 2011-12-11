package fi.evident.dojolisp.asm;

import fi.evident.dojolisp.eval.Environment;

import java.util.ArrayList;
import java.util.EnumMap;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class VM {
    
    private final EnumMap<Register, Object> registers = new EnumMap<Register, Object>(Register.class);
    private final Instructions instructions;
    private final ArrayList<Object> stack = new ArrayList<Object>();

    public VM(Instructions instructions, Environment env) {
        this.instructions = requireNonNull(instructions);
        registers.put(Register.ENV, env);
        registers.put(Register.PC, 0);
    }

    public Object run() {
        while (true) {
            int pc = (Integer) get(Register.PC);
            if (pc >= instructions.count()) break;
            OpCode op = instructions.get(pc);
            set(Register.PC, pc+1);
            
            op.execute(this);
        }
        
        return registers.get(Register.VAL);
    }

    public Object get(Register register) {
        return registers.get(register);
    }
    
    public void set(Register register, Object value) {
        registers.put(register, value);
    }
    
    public void jump(Label label) {
        set(Register.PC, instructions.labelOffSet(label));
    }
    
    int labelOffSet(Label label) {
        return instructions.labelOffSet(label);
    }

    public void push(Object object) {
        stack.add(object);
    }
    
    public Object pop() {
        return stack.remove(stack.size() - 1);
    }

    public void save(Register... registers) {
        for (Register register : registers)
            push(get(register));
    }
    
    public void restore(Register... registers) {
        for (int i = registers.length-1; i >= 0; i--)
            set(registers[i], pop());
    }
}
