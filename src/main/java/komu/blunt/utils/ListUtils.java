package komu.blunt.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

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

}
