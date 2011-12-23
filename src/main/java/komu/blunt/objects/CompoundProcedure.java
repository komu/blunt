package komu.blunt.objects;

import komu.blunt.eval.Environment;

import static com.google.common.base.Preconditions.checkNotNull;

public final class CompoundProcedure {
    
    public final int address;
    public final Environment env;

    public CompoundProcedure(int address, Environment env) {
        this.address = address;
        this.env = checkNotNull(env);
    }

    @Override
    public String toString() {
        return "<#CompoundProcedure " + address + ">";
    }
}
