package komu.blunt.analyzer;

import komu.blunt.objects.Symbol;

import java.util.HashMap;

final class IdentifierMapping {
    
    private final IdentifierMapping parent;
    private final HashMap<Symbol, Symbol> mappings = new HashMap<Symbol, Symbol>();
    
    public IdentifierMapping() {
        this.parent = null;    
    }
    
    private IdentifierMapping(IdentifierMapping parent) {
        this.parent = parent;
    }
    
    public Symbol get(Symbol var) {
        for (IdentifierMapping mapping = this; mapping != null; mapping = mapping.parent) {
            Symbol sym = mapping.mappings.get(var);
            if (sym != null)
                return sym;
        }

        return var;
    }

    public void put(Symbol oldName, Symbol newName) {
        Symbol old = mappings.put(oldName, newName);
        if (old != null)
            throw new IllegalArgumentException("duplicate mapping for: " + oldName);
    }
    
    public IdentifierMapping extend() {
        return new IdentifierMapping(this);
    }
}
