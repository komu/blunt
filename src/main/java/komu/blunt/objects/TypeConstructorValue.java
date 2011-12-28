package komu.blunt.objects;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TypeConstructorValue implements Comparable<TypeConstructorValue> {
    
    public final String name;
    public final Object[] items;
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public TypeConstructorValue(String name) {
        this(name, EMPTY_ARRAY);
    }

    public TypeConstructorValue(String name, Object[] items) {
        this.name = checkNotNull(name);
        this.items = checkNotNull(items);
    }

    @Override
    public String toString() {
        if (items.length == 0) {
            return name;
        }

        StringBuilder sb = new StringBuilder();

        if (name.equals("()")) {
            // TODO: unit is not really handled here for now
            return "()";
        } else if (isTuple()) {
            sb.append('(');
            for (int i = 0; i < items.length; i++) {
                if (i != 0) sb.append(", ");
    
                sb.append(items[i]);
            }
            sb.append(')');
        } else {
            sb.append('(').append(name);
            
            for (Object param : items)
                sb.append(' ').append(param);

            sb.append(')');
        }
        
        return sb.toString();
    }

    public boolean isTuple() {
        return name.startsWith("(,");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        
        if (obj instanceof TypeConstructorValue) {
            TypeConstructorValue rhs = (TypeConstructorValue) obj;
            
            return name.equals(rhs.name)
                && Arrays.equals(items, rhs.items);
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(items);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(TypeConstructorValue o) {
        assert name.equals(o.name);
        assert items.length == o.items.length;
        
        for (int i = 0; i < items.length; i++) {
            Comparable lhs = (Comparable) items[i];
            Comparable rhs = (Comparable) o.items[i];
            int c = lhs.compareTo(rhs);
            if (c != 0)
                return c;
        }
        return 0;
    }
}
