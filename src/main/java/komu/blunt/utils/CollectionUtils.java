package komu.blunt.utils;

import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CollectionUtils {

    public static <T> List<T> append(List<? extends T> xs, List<? extends T> ys) {
        List<T> result = new ArrayList<T>(xs.size() + ys.size());
        result.addAll(xs);
        result.addAll(ys);
        return result;
    }

    public static <T> List<T> append(List<? extends T> xs, List<? extends T> ys, List<? extends T> zs) {
        List<T> result = new ArrayList<T>(xs.size() + ys.size() + zs.size());
        result.addAll(xs);
        result.addAll(ys);
        result.addAll(zs);
        return result;
    }


    public static <T> Set<T> intersection(Iterable<Set<T>> vss) {
        Iterator<Set<T>> it = vss.iterator();

        if (!it.hasNext()) return emptySet();

        Set<T> result = new HashSet<T>(it.next());
        while (it.hasNext())
            result.retainAll(it.next());

        return result;
    }

}
