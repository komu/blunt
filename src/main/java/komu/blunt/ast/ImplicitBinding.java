package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.toSchemes;
import static komu.blunt.types.checker.TypeUtils.getTypeVariables;
import static komu.blunt.utils.CollectionUtils.intersection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import komu.blunt.objects.Symbol;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Kind;
import komu.blunt.types.Predicate;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Type;
import komu.blunt.types.TypeVariable;
import komu.blunt.types.checker.Assumptions;
import komu.blunt.types.checker.TypeCheckResult;
import komu.blunt.types.checker.TypeChecker;
import komu.blunt.types.checker.TypeCheckingVisitor;
import komu.blunt.types.checker.TypeUtils;

public final class ImplicitBinding {
    public final Symbol name;
    public final ASTExpression expr;

    public ImplicitBinding(Symbol name, ASTExpression expr) {
        this.name = checkNotNull(name);
        this.expr = checkNotNull(expr);
    }

    @Override
    public String toString() {
        return "[" + name + " " + expr + "]";
    }

    public static TypeCheckResult<Assumptions> typeCheck(List<ImplicitBinding> bs, ClassEnv classEnv, TypeChecker typeChecker, Assumptions as) {
        List<Type> ts = typeChecker.newTVars(bs.size(), Kind.STAR);
        Assumptions as2 = Assumptions.from(names(bs), toSchemes(ts)).join(as);

        List<Predicate> pss = new ArrayList<Predicate>();

        TypeCheckingVisitor checker = new TypeCheckingVisitor(classEnv, typeChecker);
        for (int i = 0; i < ts.size(); i++) {
            TypeCheckResult<Type> res = checker.typeCheck(bs.get(i).expr, as.join(as2));
            typeChecker.unify(res.value, ts.get(i));
            pss.addAll(res.predicates);
        }
        List<Predicate> ps2 = TypeUtils.apply(typeChecker.getSubstitution(), pss);
        List<Type> ts2 = TypeUtils.apply(typeChecker.getSubstitution(), ts);
        Set<TypeVariable> fs = TypeUtils.getTypeVariables(as.apply(typeChecker.getSubstitution()));
        
        List<Set<TypeVariable>> vss = new ArrayList<Set<TypeVariable>>(ts2.size());
        Set<TypeVariable> gs = new HashSet<TypeVariable>();
        for (Type t : ts2) {
            Set<TypeVariable> vars = getTypeVariables(t);
            vss.add(vars);
            gs.addAll(vars);
        }
        
        gs.removeAll(fs);

        Pair<List<Predicate>, List<Predicate>> split = split(classEnv, fs, intersection(vss), ps2);
        List<Predicate> ds = split.first;
        List<Predicate> rs = split.second;


        // TODO: restricted
        /*
                       if restricted bs then
                           let gs'  = gs \\ tv rs
                               scs' = map (quantify gs' . ([]:=>)) ts'
                           in return (ds++rs, zipWith (:>:) is scs')
                         else
                           let scs' = map (quantify gs . (rs:=>)) ts'
                           in return (ds, zipWith (:>:) is scs')
        */

        List<Scheme> scs2 = new ArrayList<Scheme>(ts2.size());
        for (Type t : ts2)
            scs2.add(quantify(gs, new Qualified<Type>(rs, t)));

        return new TypeCheckResult<Assumptions>(ds, Assumptions.from(names(bs), scs2));
    }

    private static Pair<List<Predicate>, List<Predicate>> split(ClassEnv classEnv, Set<TypeVariable> fs, Set<TypeVariable> gs, List<Predicate> ps) {
        List<Predicate> ps2 = classEnv.reduce(ps);
        List<Predicate> ds = new ArrayList<Predicate>();
        List<Predicate> rs = new ArrayList<Predicate>();
        for (Predicate p : ps2)
            if (fs.containsAll(getTypeVariables(p)))
                ds.add(p);
            else
                rs.add(p);

        // TODO: defaulted
        return new Pair<List<Predicate>,List<Predicate>>(ds, rs);
    }

    private static class Pair<A,B> {
        private final A first;
        private final B second;
        
        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }

    private static List<Symbol> names(List<ImplicitBinding> bs) {
        List<Symbol> names = new ArrayList<Symbol>(bs.size());
        for (ImplicitBinding b : bs)
            names.add(b.name);
        return names;
    }
}
