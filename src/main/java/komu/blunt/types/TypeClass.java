package komu.blunt.types;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public final class TypeClass {

    final List<String> superClasses;
    final List<ClassInstance> instances = new ArrayList<>();
    
    public TypeClass(List<String> superClasses) {
        this.superClasses = new ArrayList<>(superClasses);
    }

    public void addInstance(Qualified<Predicate> qual) {
        instances.add(new ClassInstance(qual));
    }
    
    public Iterable<Predicate> instancePredicates() {
        return Iterables.transform(instances, new Function<ClassInstance, Predicate>() {
            @Override
            public Predicate apply(ClassInstance instance) {
                return instance.qual.value;
            }
        });
    }
}
