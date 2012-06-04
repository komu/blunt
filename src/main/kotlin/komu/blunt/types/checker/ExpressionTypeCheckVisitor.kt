package komu.blunt.types.checker

import kotlin.util.*

import komu.blunt.ast.*
import komu.blunt.objects.Symbol
import komu.blunt.types.*

import java.util.ArrayList
import java.util.List

class ExpressionTypeCheckVisitor(private val tc: TypeChecker) {

    fun typeCheck(exp: ASTExpression?, ctx: Assumptions): TypeCheckResult<Type> =
      when (exp) {
        is ASTApplication -> visit(exp, ctx)
        is ASTConstant    -> visit(exp, ctx)
        is ASTLambda      -> visit(exp, ctx)
        is ASTLet         -> visit(exp, ctx)
        is ASTLetRec      -> visit(exp, ctx)
        is ASTSequence    -> visit(exp, ctx)
        is ASTSet         -> visit(exp, ctx)
        is ASTVariable    -> visit(exp, ctx)
        is ASTConstructor -> visit(exp, ctx)
        is ASTCase        -> visit(exp, ctx)
        else              -> throw Exception("unknown exp $exp")
      }

    private fun visit(application: ASTApplication, ass: Assumptions): TypeCheckResult<Type> {
        val te = typeCheck(application.func, ass)
        val tf = typeCheck(application.arg, ass);

        val t = tc.newTVar()

        tc.unify(functionType(tf.value, t), te.value)

        val result = TypeCheckResult.builder<Type>()
        result.addPredicates(te.predicates)
        result.addPredicates(tf.predicates)
        return result.build(t)
    }

    private fun visit(constant: ASTConstant, ass: Assumptions): TypeCheckResult<Type> =
        TypeCheckResult.of(constant.valueType())

    private fun visit(lambda: ASTLambda, ass: Assumptions): TypeCheckResult<Type> {
        val argumentType = tc.newTVar()

        val as2 = Assumptions.singleton(lambda.argument, Scheme.fromType(argumentType).sure())

        val result = typeCheck(lambda.body, ass.join(as2))

        return TypeCheckResult.of(functionType(argumentType, result.value).sure(), result.predicates)
    }

    private fun visit(let: ASTLet, ass: Assumptions): TypeCheckResult<Type> {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val result = TypeCheckResult.builder<Type>()

        val arg = let.bindings.first().name
        val exp = let.bindings.first().expr

        val expResult = typeCheck(exp, ass)
        result.addPredicates(expResult.predicates)

        val as2 = Assumptions.singleton(arg, Scheme.fromType(expResult.value).sure())

        val bodyResult = typeCheck(let.body, ass.join(as2))
        result.addPredicates(bodyResult.predicates)

        return result.build(bodyResult.value)
    }

    private fun visit(letRec: ASTLetRec, ass: Assumptions): TypeCheckResult<Type> {
        val bindGroup = BindGroup(ArrayList<ExplicitBinding>(), letRec.bindings)

        val rs = tc.typeCheckBindGroup(bindGroup, ass)

        return typeCheck(letRec.body, ass.join(rs.value))
    }

    private fun visit(sequence: ASTSequence, ass: Assumptions): TypeCheckResult<Type> {
        val predicates = ArrayList<Predicate>()

        for (val exp in sequence.allButLast())
            predicates.addAll(typeCheck(exp, ass).predicates)

        return typeCheck(sequence.last(), ass).withAddedPredicates(predicates).sure()
    }

    private fun visit(set: ASTSet, ass: Assumptions): TypeCheckResult<Type> =
        //// TODO: assume sets is always correct since it's auto-generated
        TypeCheckResult.of(BasicType.UNIT)

    private fun visit(variable: ASTVariable, ass: Assumptions): TypeCheckResult<Type> {
        val scheme = ass.find(variable.name)
        val inst = tc.freshInstance(scheme)
        return TypeCheckResult.of(inst.value.sure(), inst.predicates.sure())
    }

    private fun visit(constructor: ASTConstructor, ass: Assumptions): TypeCheckResult<Type> {
        val ctor = tc.findConstructor(constructor.name)

        val inst = tc.freshInstance(ctor.scheme)
        return TypeCheckResult.of(inst.value.sure(), inst.predicates.sure())
    }

    private fun visit(astCase: ASTCase, ass: Assumptions): TypeCheckResult<Type> {
        val expResult = typeCheck(astCase.exp, ass)

        val typ = tc.newTVar()

        val result = TypeCheckResult.builder<Type>()

        for (val alt in astCase.alternatives) {
            val patternResult = tc.typeCheck(alt.pattern)

            tc.unify(expResult.value.sure(), patternResult.value.sure())
            result.addPredicates(patternResult.predicates)

            val valueResult = tc.typeCheck(alt.value, patternResult.ass.join(ass))
            tc.unify(typ, valueResult.value)
            result.addPredicates(valueResult.predicates)
        }

        return result.build(typ)
    }
}
