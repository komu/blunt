package fi.evident.dojolisp.asm;

import static fi.evident.dojolisp.utils.Objects.requireNonNull;

public final class Label extends OpCode {
    
    private final String name;
    
    Label(String name) {
        this.name = requireNonNull(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof Label) {
            Label rhs = (Label) obj;
            return name.equals(rhs.name);
        }

        return false;
    }

    @Override
    public void execute(VM vm) {
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
