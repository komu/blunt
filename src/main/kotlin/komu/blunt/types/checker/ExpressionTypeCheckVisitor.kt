package komu.blunt.types.checker

import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.*

import java.util.ArrayList
import java.util.List

import komu.blunt.types.Type.functionType

class ExpressionTypeCheckVisitor(private val tc: TypeChecker) : ASTVisitor<Assumptions, TypeCheckResult<Type?>> {

    private fun typeCheck(exp: ASTExpression?, ass: Assumptions?): TypeCheckResult<Type?> =
        tc.typeCheck(exp.sure(), ass.sure())

    override fun visit(application: ASTApplication?, ass: Assumptions): TypeCheckResult<Type?> {
        val te = typeCheck(application?.func, ass)
        val tf = typeCheck(application?.arg, ass);

        val t = tc.newTVar()

        tc.unify(functionType(tf.value, t).sure(), te.value.sure());

        val result = TypeCheckResult.builder<Type?>().sure()
        result.addPredicates(te.predicates)
        result.addPredicates(tf.predicates)
        return result.build(t).sure()
    }

    override fun visit(constant: ASTConstant?, ass: Assumptions): TypeCheckResult<Type?> =
        TypeCheckResult.of(constant?.valueType()).sure()

    override fun visit(lambda: ASTLambda?, ass: Assumptions): TypeCheckResult<Type?> {
        val argumentType = tc.newTVar()

        val as2 = Assumptions.singleton(lambda?.argument, argumentType.toScheme())

        val result = typeCheck(lambda?.body, ass.join(as2))

        return TypeCheckResult.of(functionType(argumentType, result.value), result.predicates).sure()
    }

    override fun visit(let: ASTLet?, ass: Assumptions): TypeCheckResult<Type?> {
        if (let?.bindings?.size() != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val result = TypeCheckResult.builder<Type?>().sure()

        val arg = let?.bindings?.get(0)?.name.sure()
        val exp = let?.bindings?.get(0)?.expr.sure()

        val expResult = typeCheck(exp, ass)
        result.addPredicates(expResult.predicates)

        val as2 = Assumptions.singleton(arg, expResult.value?.toScheme()).sure()

        val bodyResult = typeCheck(let?.body, ass.join(as2))
        result.addPredicates(bodyResult.predicates)

        return result.build(bodyResult.value).sure()
    }

    override fun visit(letRec: ASTLetRec?, ass: Assumptions): TypeCheckResult<Type?> {
        val bindGroup = BindGroup(ArrayList<ExplicitBinding?>(), letRec?.bindings)

        val rs = tc.typeCheckBindGroup(bindGroup, ass)

        return typeCheck(letRec?.body, ass.join(rs.value))
    }

    override fun visit(sequence: ASTSequence?, ass: Assumptions): TypeCheckResult<Type?> {
        val predicates = ArrayList<Predicate?>()

        for (val exp in sequence?.allButLast())
            predicates.addAll(typeCheck(exp, ass).predicates.sure())

        return typeCheck(sequence?.last(), ass).withAddedPredicates(predicates).sure()
    }

    override fun visit(set: ASTSet?, ass: Assumptions): TypeCheckResult<Type?> =
        //// TODO: assume sets is always correct since it's auto-generated
        TypeCheckResult.of(Type.UNIT).sure()

    override fun visit(variable: ASTVariable?, ass: Assumptions): TypeCheckResult<Type?> {
        val scheme = ass.find(variable?.`var`).sure()
        val inst = tc.freshInstance(scheme)
        return TypeCheckResult.of(inst.value, inst.predicates).sure()
    }

    override fun visit(constructor: ASTConstructor?, ass: Assumptions): TypeCheckResult<Type?> {
        val ctor = tc.findConstructor(constructor?.name.sure())

        val inst = tc.freshInstance(ctor.scheme.sure())
        return TypeCheckResult.of(inst.value, inst.predicates).sure()
    }

    override fun visit(astCase: ASTCase?, ass: Assumptions): TypeCheckResult<Type?> {
        val expResult = typeCheck(astCase?.exp, ass);

        val typ = tc.newTVar()

        val result = TypeCheckResult.builder<Type?>().sure()

        for (val alt in astCase?.alternatives) {
            val patternResult = tc.typeCheck(alt?.pattern.sure())

            tc.unify(expResult.value.sure(), patternResult.value.sure())
            result.addPredicates(patternResult.predicates)

            val valueResult = tc.typeCheck(alt?.value.sure(), patternResult.`as`?.join(ass).sure())
            tc.unify(typ, valueResult.value.sure())
            result.addPredicates(valueResult.predicates)
        }

        return result.build(typ).sure()
    }
}
