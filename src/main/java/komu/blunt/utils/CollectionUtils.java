package komu.blunt.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.util.Collections.emptySet;

public final class CollectionUtils {

    private CollectionUtils() { }

    public static <T> Set<T> intersection(Iterable<Set<T>> sets) {
        Iterator<Set<T>> it = sets.iterator();

        if (!it.hasNext())
            return emptySet();

        Set<T> result = new HashSet<T>(it.next());
        while (it.hasNext())
            result.retainAll(it.next());

        return result;
    }
}
