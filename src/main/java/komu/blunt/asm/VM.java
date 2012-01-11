package komu.blunt.asm;

import komu.blunt.asm.opcodes.OpCode;
import komu.blunt.eval.Environment;
import komu.blunt.eval.RootEnvironment;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VM {
    
    public Object val;
    public Object env;
    public Object procedure;
    public Object arg;
    public int pc = 0;
    private final Instructions instructions;
    private final Deque<Object> stack = new ArrayDeque<>(4096);
    private final RootEnvironment globalEnvironment;
    public long steps = 0;

    public VM(Instructions instructions, Environment env, RootEnvironment globalEnvironment) {
        this.instructions = checkNotNull(instructions);
        this.globalEnvironment = checkNotNull(globalEnvironment);

        this.env = checkNotNull(env);
    }

    public Object run() {
        while (true) {
            if (pc >= instructions.count()) break;
            OpCode op = instructions.get(pc++);
            steps++;
            op.execute(this);
        }
        
        return val;
    }

    public Object get(Register register) {
        switch (register) {
        case VAL: return val;
        case ARG: return arg;
        case ENV: return env;
        case PC: return pc;
        case PROCEDURE: return procedure;
        }

        throw new IllegalArgumentException("unknown register: " + register);
    }
    
    public void set(Register register, Object value) {
        switch (register) {
        case VAL:
            val = value;
            break;
        case ARG:
            arg = value;
            break;
        case ENV:
            env = value;
            break;
        case PC:
            pc = (Integer) value;
            break;
        case PROCEDURE:
            procedure = value;
            break;
        default:
            throw new IllegalArgumentException("unknown register: " + register);
        }
    }
    
    public void jump(Label label) {
        this.pc = label.getAddress();
    }
    
    public void push(Object object) {
        stack.add(object);
    }
    
    public Object pop() {
        return stack.removeLast();
    }

    public void save(Register... registers) {
        for (Register register : registers)
            push(get(register));
    }
    
    public void restore(Register... registers) {
        for (int i = registers.length-1; i >= 0; i--)
            set(registers[i], pop());
    }

    public RootEnvironment getGlobalEnvironment() {
        return globalEnvironment;
    }
}
