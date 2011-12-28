package komu.blunt.types.checker;

import komu.blunt.ast.*;
import komu.blunt.eval.TypeCheckException;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;
import komu.blunt.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;
import static komu.blunt.types.checker.Unifier.mgu;
import static komu.blunt.utils.CollectionUtils.append;

public final class TypeCheckingVisitor implements ASTVisitor<Assumptions, TypeCheckResult<Type>> {
    
    private final ClassEnv classEnv;
    private Substitution substitution = Substitution.empty();
    private int typeSequence = 0;

    public TypeCheckingVisitor(ClassEnv classEnv) {
        this.classEnv = checkNotNull(classEnv);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTApplication application, Assumptions as) {
        TypeCheckResult<Type> te = typeCheck(application.func, as);
        TypeCheckResult<Type> tf = typeCheck(application.arg, as);

        Type t = newTVar(STAR);

        unify(functionType(tf.value, t), te.value);

        return new TypeCheckResult<Type>(CollectionUtils.append(te.predicates, tf.predicates), t);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstant constant, Assumptions as) {
        return new TypeCheckResult<Type>(constant.valueType());
    }

    @Override
    public TypeCheckResult<Type> visit(ASTIf ifExp, Assumptions as) {
        TypeCheckResult<Type> tyTest = typeCheck(ifExp.test, as);
        TypeCheckResult<Type> tyConsequent = typeCheck(ifExp.consequent, as);
        TypeCheckResult<Type> tyAlternative = typeCheck(ifExp.alternative, as);

        unify(tyTest.value, Type.BOOLEAN);
        unify(tyConsequent.value, tyAlternative.value);

        List<Predicate> predicates = append(tyTest.predicates, tyConsequent.predicates, tyAlternative.predicates);
        return new TypeCheckResult<Type>(predicates, tyConsequent.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLambda lambda, Assumptions as) {
        if (lambda.arguments.size() == 1) {
            Symbol arg = lambda.arguments.get(0);

            TypeVariable argumentType = newTVar(Kind.STAR);

            Assumptions as2 = Assumptions.singleton(arg, argumentType.toScheme());

            TypeCheckResult<Type> result = typeCheck(lambda.body, as.join(as2));

            return new TypeCheckResult<Type>(result.predicates, functionType(argumentType, result.value));
        } else {
            return typeCheck(lambda.rewrite(), as);
        }
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLet let, Assumptions as) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        Symbol arg = let.bindings.get(0).name;
        ASTExpression exp = let.bindings.get(0).expr;

        TypeCheckResult<Type> expResult = typeCheck(exp, as);

        Assumptions as2 = Assumptions.singleton(arg, expResult.value.toScheme());

        TypeCheckResult<Type> result = typeCheck(let.body, as.join(as2));

        return new TypeCheckResult<Type>(CollectionUtils.append(expResult.predicates, result.predicates), result.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLetRec letRec, Assumptions as) {
        BindGroup bindGroup = new BindGroup(new ArrayList<ExplicitBinding>(), letRec.bindings);

        TypeCheckResult<Assumptions> rs = typeCheckBindGroup(bindGroup, as);

        return typeCheck(letRec.body, as.join(rs.value));
    }

    private TypeCheckResult<Assumptions> typeCheckBindGroup(BindGroup bindings, Assumptions as) {
        Assumptions as2 = bindings.assumptionFromExplicitBindings();

        TypeCheckResult<Assumptions> res = typeCheckImplicits(bindings, as.join(as2));
        Assumptions as3 = res.value;
        List<Predicate> ps = typeCheckExplicits(bindings, as.join(as3.join(as2)));

        return new TypeCheckResult<Assumptions>(append(res.predicates, ps), as3.join(as2));
    }

    private TypeCheckResult<Assumptions> typeCheckImplicits(BindGroup bindings, Assumptions origAs) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        Assumptions as = Assumptions.empty();

        for (List<ImplicitBinding> bs : bindings.implicitBindings) {
            TypeCheckResult<Assumptions> res = ImplicitBinding.typeCheck(bs, this, classEnv, as.join(origAs));
            predicates.addAll(res.predicates);
            as = res.value.join(as);
        }

        return new TypeCheckResult<Assumptions>(predicates, as);
    }

    private List<Predicate> typeCheckExplicits(BindGroup bindGroup, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (ExplicitBinding b : bindGroup.explicitBindings)
            predicates.addAll(b.typeCheck(this, as));

        return predicates;
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSequence sequence, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (ASTExpression exp : sequence.allButLast())
            predicates.addAll(typeCheck(exp, as).predicates);

        TypeCheckResult<Type> result = typeCheck(sequence.last(), as);

        predicates.addAll(result.predicates);

        return new TypeCheckResult<Type>(predicates, result.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSet set, Assumptions as) {
        // TODO: assume sets is always correct since it's auto-generated
        return new TypeCheckResult<Type>(Type.UNIT);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTTuple tuple, Assumptions as) {
        // TODO: generalize tuples to type constructors
        List<Predicate> predicates = new ArrayList<Predicate>();
        List<Type> types = new ArrayList<Type>();
        
        for (ASTExpression exp : tuple.exps) {
            TypeCheckResult<Type> result = typeCheck(exp, as);
            predicates.addAll(result.predicates);
            types.add(result.value);
        }
        
        return new TypeCheckResult<Type>(predicates, tupleType(types));
    }

    @Override
    public TypeCheckResult<Type> visit(ASTVariable variable, Assumptions as) {
        Scheme scheme = as.find(variable.var);
        Qualified<Type> inst = freshInstance(scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTList list, Assumptions as) {
        // TODO: don't include node for ASTList at all
        return typeCheck(list.rewrite(), as);
    }

    public TypeCheckResult<Type> typeCheck(ASTExpression exp, Assumptions as) {
        return exp.accept(this, as);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstructor constructor, Assumptions as) {
        ConstructorDefinition ctor = DataTypeDefinitions.findConstructor(constructor.name);

        Qualified<Type> inst = freshInstance(ctor.scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    public List<Type> newTVars(final int size, final Kind kind) {
        List<Type> types = new ArrayList<Type>(size);
        for (int i = 0; i < size; i++)
            types.add(newTVar(kind));
        return types;
    }

    public Qualified<Type> freshInstance(Scheme scheme) {
        List<TypeVariable> ts = new ArrayList<TypeVariable>(scheme.kinds.size());
        for (Kind kind : scheme.kinds)
            ts.add(newTVar(kind));

        return Qualified.instantiate(ts, scheme.type);
    }

    public TypeVariable newTVar(Kind kind) {
        return new TypeVariable(typeName(typeSequence++), kind);
    }

    private static String typeName(int index) {
        if (index < 5) {
            return String.valueOf((char) ('a' + index));
        } else {
            return "t" + (index-5);
        }
    }

    public void unify(Type t1, Type t2) {
        try {
            Substitution u = mgu(t1.apply(substitution), t2.apply(substitution));
            substitution = u.compose(substitution);
        } catch (UnificationException e) {
            throw new TypeCheckException(e);
        }
    }

    public Substitution getSubstitution() {
        return substitution;
    }
}

