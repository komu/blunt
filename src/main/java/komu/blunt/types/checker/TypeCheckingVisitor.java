package komu.blunt.types.checker;

import komu.blunt.ast.*;
import komu.blunt.objects.Symbol;
import komu.blunt.types.*;
import komu.blunt.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static komu.blunt.types.Kind.STAR;
import static komu.blunt.types.Type.functionType;
import static komu.blunt.types.Type.tupleType;
import static komu.blunt.utils.CollectionUtils.append;

public final class TypeCheckingVisitor implements ASTVisitor<TypeCheckingContext, TypeCheckResult<Type>> {

    @Override
    public TypeCheckResult<Type> visit(ASTApplication application, TypeCheckingContext ctx) {
        TypeCheckResult<Type> te = typeCheck(application.func, ctx);
        TypeCheckResult<Type> tf = typeCheck(application.arg, ctx);

        Type t = ctx.newTVar(STAR);

        ctx.unify(functionType(tf.value, t), te.value);

        return new TypeCheckResult<Type>(CollectionUtils.append(te.predicates, tf.predicates), t);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstant constant, TypeCheckingContext ctx) {
        return new TypeCheckResult<Type>(constant.valueType());
    }

    @Override
    public TypeCheckResult<Type> visit(ASTIf ifExp, TypeCheckingContext ctx) {
        TypeCheckResult<Type> tyTest = typeCheck(ifExp.test, ctx);
        TypeCheckResult<Type> tyConsequent = typeCheck(ifExp.consequent, ctx);
        TypeCheckResult<Type> tyAlternative = typeCheck(ifExp.alternative, ctx);

        ctx.unify(tyTest.value, Type.BOOLEAN);
        ctx.unify(tyConsequent.value, tyAlternative.value);

        List<Predicate> predicates = append(tyTest.predicates, tyConsequent.predicates, tyAlternative.predicates);
        return new TypeCheckResult<Type>(predicates, tyConsequent.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLambda lambda, TypeCheckingContext ctx) {
        if (lambda.arguments.size() == 1) {
            Symbol arg = lambda.arguments.get(0);

            TypeVariable argumentType = ctx.newTVar(Kind.STAR);

            Assumptions as2 = Assumptions.singleton(arg, argumentType.toScheme());

            TypeCheckResult<Type> result = typeCheck(lambda.body, ctx.extend(as2));

            return new TypeCheckResult<Type>(result.predicates, functionType(argumentType, result.value));
        } else {
            return typeCheck(lambda.rewrite(), ctx);
        }
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLet let, TypeCheckingContext ctx) {
        if (let.bindings.size() != 1)
            throw new UnsupportedOperationException("multi-var let is not supported");

        Symbol arg = let.bindings.get(0).name;
        ASTExpression exp = let.bindings.get(0).expr;

        TypeCheckResult<Type> expResult = typeCheck(exp, ctx);

        Assumptions as2 = Assumptions.singleton(arg, expResult.value.toScheme());

        TypeCheckResult<Type> result = typeCheck(let.body, ctx.extend(as2));

        return new TypeCheckResult<Type>(CollectionUtils.append(expResult.predicates, result.predicates), result.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTLetRec letRec, TypeCheckingContext ctx) {
        TypeCheckResult<Assumptions> rs = new BindGroup(new ArrayList<ExplicitBinding>(), letRec.bindings).typeCheckBindGroup(ctx);

        return typeCheck(letRec.body, ctx.extend(rs.value));
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSequence sequence, TypeCheckingContext ctx) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        for (ASTExpression exp : sequence.allButLast())
            predicates.addAll(typeCheck(exp, ctx).predicates);

        TypeCheckResult<Type> result = typeCheck(sequence.last(), ctx);

        predicates.addAll(result.predicates);

        return new TypeCheckResult<Type>(predicates, result.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTSet set, TypeCheckingContext ctx) {
        // TODO: assume sets is always correct since it's auto-generated
        return new TypeCheckResult<Type>(Type.UNIT);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTTuple tuple, TypeCheckingContext ctx) {
        // TODO: generalize tuples to type constructors
        List<Predicate> predicates = new ArrayList<Predicate>();
        List<Type> types = new ArrayList<Type>();
        
        for (ASTExpression exp : tuple.exps) {
            TypeCheckResult<Type> result = typeCheck(exp, ctx);
            predicates.addAll(result.predicates);
            types.add(result.value);
        }
        
        return new TypeCheckResult<Type>(predicates, tupleType(types));
    }

    @Override
    public TypeCheckResult<Type> visit(ASTVariable variable, TypeCheckingContext ctx) {
        Scheme scheme = ctx.find(variable.var);
        Qualified<Type> inst = ctx.freshInstance(scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTList list, TypeCheckingContext ctx) {
        // TODO: don't include node for ASTList at all
        return typeCheck(list.rewrite(), ctx);
    }

    public TypeCheckResult<Type> typeCheck(ASTExpression exp, TypeCheckingContext ctx) {
        return exp.accept(this, ctx);
    }

    @Override
    public TypeCheckResult<Type> visit(ASTConstructor constructor, TypeCheckingContext ctx) {
        ConstructorDefinition ctor = DataTypeDefinitions.findConstructor(constructor.name);

        Qualified<Type> inst = ctx.freshInstance(ctor.scheme);
        return new TypeCheckResult<Type>(inst.predicates, inst.value);
    }
}

