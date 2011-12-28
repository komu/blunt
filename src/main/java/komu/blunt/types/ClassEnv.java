package komu.blunt.types;

import komu.blunt.eval.TypeCheckException;
import komu.blunt.types.checker.Substitution;
import komu.blunt.types.checker.TypeUtils;
import komu.blunt.types.checker.UnificationException;
import komu.blunt.types.checker.Unifier;
import komu.blunt.utils.Pair;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Predicate.isIn;
import static komu.blunt.types.Type.tupleType;
import static komu.blunt.types.TypeVariable.tyVar;
import static komu.blunt.types.checker.TypeUtils.getTypeVariables;

public final class ClassEnv {
    
    private final Map<String,TypeClass> classes = new HashMap<String, TypeClass>();
    private final List<Type> defaults = newArrayList(Type.INTEGER);

    public ClassEnv() {
        addCoreClasses();
        addNumClasses();
        addDefaultInstances();
    }

    private Collection<String> getSuperClasses(String name) {
        return getClass(name).superClasses;
    }

    private Collection<ClassInstance> getInstances(String name) {
        return getClass(name).instances;
    }

    private void addCoreClasses() {
        addClass("Eq");
        addClass("Ord", "Eq");
        addClass("Show");
        addClass("Read");
        addClass("Bounded");
        addClass("Enum");
        addClass("Functor");
        addClass("Monad");
    }

    private void addNumClasses() {
        addClass("Num", "Eq", "Show");
        addClass("Real", "Num", "Ord");
        addClass("Fractional", "Num");
        addClass("Integral", "Real", "Enum");
        addClass("RealFrac", "Real", "Fractional");
        addClass("Floating", "Fractional");
        addClass("RealFloat", "RealFrac", "Floating");
    }
    
    private void addDefaultInstances() {
        addInstance(isIn("Num", Type.INTEGER));
        addInstance(isIn("Eq", Type.INTEGER));
        addInstance(isIn("Eq", Type.BOOLEAN));
        addInstance(isIn("Eq", Type.STRING));
        addInstance(isIn("Eq", Type.UNIT));

        addInstance(isIn("Ord", Type.UNIT));
        addInstance(isIn("Ord", Type.BOOLEAN));
        addInstance(isIn("Ord", Type.INTEGER));
        addInstance(isIn("Ord", Type.STRING));

        addInstance(asList(isIn("Ord", tyVar("a", STAR)),
                           isIn("Ord", tyVar("b", STAR))),
                    isIn("Ord", tupleType(tyVar("a", STAR), tyVar("b", STAR))));

        addInstance(asList(isIn("Eq", tyVar("a", STAR)),
                           isIn("Eq", tyVar("b", STAR))),
                    isIn("Eq", tupleType(tyVar("a", STAR), tyVar("b", STAR))));

        addInstance(asList(isIn("Eq", tyVar("a", STAR))),
                    isIn("Eq", Type.listType(tyVar("a", STAR))));

        addInstance(asList(isIn("Eq", tyVar("a", STAR))),
                    isIn("Eq", Type.genericType("Maybe", tyVar("a", STAR))));
    }

    public void addInstance(Predicate predicate) {
        addInstance(Collections.<Predicate>emptyList(), predicate);
    }
    
    public void addInstance(List<Predicate> predicates, Predicate predicate) {
        TypeClass cl = getClass(predicate.className);

        if (predicate.overlapsAny(cl.instancePredicates()))
            throw new RuntimeException("overlapping instances");

        cl.addInstance(new Qualified<Predicate>(predicates, predicate));
    }

    public void addClass(String name, String... superClasses) {
        addClass(name, new TypeClass(Arrays.asList(superClasses)));
    }

    public void addClass(String name, TypeClass cl) {
        if (defined(name))
            throw new RuntimeException("class already defined: " + name);

        if (!allClassNames().containsAll(cl.superClasses))
            throw new RuntimeException("unknown superclass");
        
        classes.put(checkNotNull(name), checkNotNull(cl));
    }

    private List<Predicate> bySuper(Predicate predicate) {
        List<Predicate> result = new ArrayList<Predicate>();
        result.add(predicate);

        for (String superName : getSuperClasses(predicate.className))
            result.addAll(bySuper(Predicate.isIn(superName, predicate.type)));
        
        return result;
    }

    @Nullable
    private List<Predicate> byInstance(Predicate predicate) {
        for (ClassInstance it : getInstances(predicate.className)) {
            try {
                Substitution s = Unifier.matchPredicate(it.getPredicate(), predicate);
                return TypeUtils.applySubstitution(s, it.getPredicates());
            } catch (UnificationException e) {
                // skip
            }
        }
        
        return null;
    }

    private boolean entails(Collection<Predicate> ps, Predicate p) {
        for (Predicate pp : ps)
            if (bySuper(pp).contains(p))
                return true;

        List<Predicate> qs = byInstance(p);
        if (qs == null) {
            return false;
        } else {
            for (Predicate q : qs)
                if (!entails(ps, q))
                    return false;
            return true;
        }
    }

    private List<Predicate> toHfns(List<Predicate> predicates) {
        List<Predicate> result = new ArrayList<Predicate>();

        for (Predicate predicate : predicates)
            if (predicate.inHnf()) {
                result.add(predicate);
            } else {
                List<Predicate> qs = byInstance(predicate);
                if (qs != null)
                    result.addAll(toHfns(qs));
                else
                    throw new TypeCheckException("could not find instance of " + predicate.className + " for type " + predicate.type);
            }

        return result;
    }
    
    private List<Predicate> simplify(List<Predicate> ps) {
        Set<Predicate> combinedPredicates = new HashSet<Predicate>();
        List<Predicate> rs = new ArrayList<Predicate>();

        for (Predicate p : ps) {
            if (!entails(combinedPredicates, p)) {
                combinedPredicates.add(p);
                rs.add(p);
            }
        }

        return rs;
    }
    
    public List<Predicate> reduce(List<Predicate> ps) {
        return simplify(toHfns(ps));
    }

    public Pair<List<Predicate>, List<Predicate>> split(Set<TypeVariable> fixedVariables,
                                                        Set<TypeVariable> quantifyVariables,
                                                        List<Predicate> originalPredicates) {
        List<Predicate> deferredPredicates = new ArrayList<Predicate>();
        List<Predicate> retainedPredicates = new ArrayList<Predicate>();

        for (Predicate predicate : reduce(originalPredicates))
            if (fixedVariables.containsAll(getTypeVariables(predicate)))
                deferredPredicates.add(predicate);
            else
                retainedPredicates.add(predicate);

        // TODO: defaulted
        return new Pair<List<Predicate>,List<Predicate>>(deferredPredicates, retainedPredicates);
    }

    private Set<String> allClassNames() {
        return classes.keySet();
    }

    public boolean defined(String name) {
        return classes.containsKey(name);
    }

    private TypeClass getClass(String name) {
        TypeClass cl = classes.get(name);
        if (cl != null)
            return cl;
        else
            throw new RuntimeException("unknown class: " + name);
    }
}
