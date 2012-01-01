package komu.blunt.types.checker;

import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Type.functionType;

public final class ExpressionTypeCheckVisitor implements ASTVisitor<Assumptions, TypeCheckResult<Type>> {
    
    private final TypeChecker tc;

    public ExpressionTypeCheckVisitor(TypeChecker tc) {
        this.tc = checkNotNull(tc);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTApplication application, Assumptions as) {

        TypeCheckResult<Type> te = typeCheck(application.func, as);

        TypeCheckResult<Type> tf = typeCheck(application.arg, as);

        Type t = tc.newTVar(STAR);

        tc.unify(functionType(tf.value, t), te.value);

        TypeCheckResult.Builder<Type> result = TypeCheckResult.builder();
        result.addPredicates(te.predicates);
        result.addPredicates(tf.predicates);
        return result.build(t);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstant constant, Assumptions as) {
        return TypeCheckResult.of(constant.valueType());
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLambda lambda, Assumptions as) {
        if (lambda.arguments.size() == 1) {
            Symbol arg = lambda.arguments.get(0);

            TypeVariable argumentType = tc.newTVar(Kind.STAR);

            Assumptions as2 = Assumptions.singleton(arg, argumentType.toScheme());

            TypeCheckResult<Type> result = typeCheck(lambda.body, as.join(as2));

            return TypeCheckResult.of(functionType(argumentType, result.value), result.predicates);
        } else {
            return typeCheck(lambda.rewrite(), as);
        }
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLet let, Assumptions as) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        TypeCheckResult.Builder<Type> result = TypeCheckResult.builder();

        Symbol arg = let.bindings.get(0).name;
        ASTExpression exp = let.bindings.get(0).expr;

        TypeCheckResult<Type> expResult = typeCheck(exp, as);
        result.addPredicates(expResult.predicates);

        Assumptions as2 = Assumptions.singleton(arg, expResult.value.toScheme());

        TypeCheckResult<Type> bodyResult = typeCheck(let.body, as.join(as2));
        result.addPredicates(bodyResult.predicates);

        return result.build(bodyResult.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLetRec letRec, Assumptions as) {
        BindGroup bindGroup = new BindGroup(new ArrayList<ExplicitBinding>(), letRec.bindings);

        TypeCheckResult<Assumptions> rs = tc.typeCheckBindGroup(bindGroup, as);

        return typeCheck(letRec.body, as.join(rs.value));
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSequence sequence, Assumptions as) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (ASTExpression exp : sequence.allButLast())
            predicates.addAll(typeCheck(exp, as).predicates);

        return typeCheck(sequence.last(), as).withAddedPredicates(predicates);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSet set, Assumptions as) {
        // TODO: assume sets is always correct since it's auto-generated
        return TypeCheckResult.of(Type.UNIT);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTVariable variable, Assumptions as) {
        Scheme scheme = as.find(variable.var);
        Qualified<Type> inst = tc.freshInstance(scheme);
        return TypeCheckResult.of(inst.value, inst.predicates);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTList list, Assumptions as) {
        // TODO: don't include node for ASTList at all
        return typeCheck(list.rewrite(), as);
    }

    private TypeCheckResult<Type> typeCheck(ASTExpression exp, Assumptions as) {
        return tc.typeCheck(exp, as);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstructor constructor, Assumptions as) {
        ConstructorDefinition ctor = tc.findConstructor(constructor.name);

        Qualified<Type> inst = tc.freshInstance(ctor.scheme);
        return TypeCheckResult.of(inst.value, inst.predicates);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTCase astCase, Assumptions as) {
        TypeCheckResult<Type> expResult = tc.typeCheck(astCase.exp, as);

        Type type = tc.newTVar();

        TypeCheckResult.Builder<Type> result = TypeCheckResult.builder();

        for (ASTAlternative alt : astCase.alternatives) {
            PatternTypeCheckResult<Type> patternResult = tc.typeCheck(alt.pattern);

            tc.unify(expResult.value, patternResult.value);
            result.addPredicates(patternResult.predicates);
            
            TypeCheckResult<Type> valueResult = tc.typeCheck(alt.value, patternResult.as.join(as));
            tc.unify(type, valueResult.value);
            result.addPredicates(valueResult.predicates);
        }

        return result.build(type);
    }
}
