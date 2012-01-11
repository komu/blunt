package komu.blunt.core;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public final class PatternPath {
    
    public static final PatternPath EMPTY = new PatternPath(null, -1);
    private final PatternPath parent;
    private final int index;
    
    private PatternPath(PatternPath parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    public PatternPath extend(int index) {
        return new PatternPath(this, index);
    }
    
    public List<Integer> indices() {
        List<Integer> indices = new ArrayList<>();
        
        for (PatternPath p = this; p.parent != null; p = p.parent) 
            indices.add(p.index);

        return Lists.reverse(indices);
    }

    @Override
    public String toString() {
        return indices().toString();
    }
}
