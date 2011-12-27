package komu.blunt.ast;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Qualified.quantify;
import static komu.blunt.types.Type.toSchemes;
import static komu.blunt.types.TypeUtils.getTypeVariables;
import static komu.blunt.utils.CollectionUtils.intersection;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import komu.blunt.objects.Symbol;
import komu.blunt.types.Assumptions;
import komu.blunt.types.ClassEnv;
import komu.blunt.types.Kind;
import komu.blunt.types.Predicate;
import komu.blunt.types.Qualified;
import komu.blunt.types.Scheme;
import komu.blunt.types.Substitution;
import komu.blunt.types.Type;
import komu.blunt.types.TypeCheckResult;
import komu.blunt.types.TypeChecker;
import komu.blunt.types.TypeCheckingContext;
import komu.blunt.types.TypeUtils;
import komu.blunt.types.TypeVariable;

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

    public static TypeCheckResult<Assumptions> typeCheck(List<ImplicitBinding> bs, ClassEnv ce, TypeChecker tc, Assumptions as) {
        List<Type> ts = tc.newTVars(bs.size(), Kind.STAR);
        List<Symbol> is  = names(bs);
        List<Scheme> scs = toSchemes(ts);
        Assumptions as2 = Assumptions.from(is, scs).join(as);
        List<ASTExpression> altss = exps(bs);

        List<Predicate> pss = new ArrayList<Predicate>();
        for (int i = 0; i < ts.size(); i++) {
            TypeCheckResult<Type> res = altss.get(i).typeCheck(new TypeCheckingContext(ce, tc, as2));
            tc.unify(res.value, ts.get(i));
            pss.addAll(res.predicates);
        }

        Substitution s = tc.getSubstitution();

        List<Predicate> ps2 = TypeUtils.apply(s, pss);
        List<Type> ts2 = TypeUtils.apply(s, ts);
        Set<TypeVariable> fs = getTypeVariables(as.apply(s));
        
        List<Set<TypeVariable>> vss = new ArrayList<Set<TypeVariable>>(ts2.size());
        Set<TypeVariable> gs = new LinkedHashSet<TypeVariable>();
        for (Type t : ts2) {
            Set<TypeVariable> vars = getTypeVariables(t);
            vss.add(vars);
            gs.addAll(vars);
        }
        
        gs.removeAll(fs);

        Pair<List<Predicate>, List<Predicate>> split = split(ce, tc, fs, intersection(vss), ps2);
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

        return new TypeCheckResult<Assumptions>(ds, Assumptions.from(is, scs2));
    }

    private static Pair<List<Predicate>, List<Predicate>> split(ClassEnv ce, TypeChecker tc, Set<TypeVariable> fs, Set<TypeVariable> gs, List<Predicate> ps) {
        List<Predicate> ps2 = ce.reduce(ps);
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

    private static List<ASTExpression> exps(List<ImplicitBinding> bs) {
        List<ASTExpression> exps = new ArrayList<ASTExpression>(bs.size());
        for (ImplicitBinding b : bs)
            exps.add(b.expr);
        return exps;
    }

    private static List<Symbol> names(List<ImplicitBinding> bs) {
        List<Symbol> names = new ArrayList<Symbol>(bs.size());
        for (ImplicitBinding b : bs)
            names.add(b.name);
        return names;
    }
}
