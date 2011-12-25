package komu.blunt.objects;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Tuple {
    
    public final Object[] items;

    public Tuple(Object[] items) {
        this.items = checkNotNull(items);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append('(');
        for (int i = 0; i < items.length; i++) {
            if (i != 0) sb.append(", ");

            sb.append(items[i]);
        }
        sb.append(')');
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof Tuple) {
            Tuple rhs = (Tuple) obj;
            
            return Arrays.equals(items, rhs.items);
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }
}
