package komu.blunt.asm;

import static komu.blunt.utils.Objects.requireNonNull;

public final class Label {
    
    private final String name;
    private int address = -1;
    
    Label(String name) {
        this.name = requireNonNull(name);
    }

    void setAddress(int address) {
        if (address < 0) throw new IllegalArgumentException("negative address");
        
        if (this.address != -1)
            throw new IllegalStateException("address already set");
        
        this.address = address;
    }
    
    int getAddress() {
        if (address == -1) throw new IllegalStateException("address not initialized");

        return address;
    }

    @Override
    public String toString() {
        return name;
    }
}
