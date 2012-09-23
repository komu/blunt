package komu.blunt.types.checker

import java.util.ArrayList
import komu.blunt.ast.*
import komu.blunt.types.*
import komu.blunt.utils.init

class ExpressionTypeCheckVisitor(private val tc: TypeChecker) {

    fun typeCheck(exp: ASTExpression, ctx: Assumptions): TypeCheckResult<Type> =
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

        return TypeCheckResult.of(t, te.predicates, tf.predicates)
    }

    private fun visit(constant: ASTConstant, ass: Assumptions): TypeCheckResult<Type> =
        TypeCheckResult.of(constant.valueType())

    private fun visit(lambda: ASTLambda, ass: Assumptions): TypeCheckResult<Type> {
        val argumentType = tc.newTVar()
        val as2 = Assumptions.singleton(lambda.argument, argumentType.toScheme() )
        val (resultType, predicates) = typeCheck(lambda.body, ass.join(as2))

        return TypeCheckResult.of(functionType(argumentType, resultType), predicates)
    }

    private fun visit(let: ASTLet, ass: Assumptions): TypeCheckResult<Type> {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val arg = let.bindings.first().name
        val exp = let.bindings.first().expr

        val expResult = typeCheck(exp, ass)

        val as2 = Assumptions.singleton(arg, expResult.value.toScheme())

        val bodyResult = typeCheck(let.body, ass.join(as2))

        return TypeCheckResult.of(bodyResult.value, expResult.predicates, bodyResult.predicates)
    }

    private fun visit(letRec: ASTLetRec, ass: Assumptions): TypeCheckResult<Type> {
        val bindGroup = BindGroup(ArrayList<ExplicitBinding>(), letRec.bindings)

        val rs = tc.typeCheckBindGroup(bindGroup, ass)

        return typeCheck(letRec.body, ass.join(rs.value))
    }

    private fun visit(sequence: ASTSequence, ass: Assumptions): TypeCheckResult<Type> {
        val predicates = ArrayList<Predicate>()

        for (exp in sequence.exps.init)
            predicates.addAll(typeCheck(exp, ass).predicates)

        return typeCheck(sequence.exps.last(), ass).withAddedPredicates(predicates)
    }

    private fun visit(set: ASTSet, ass: Assumptions): TypeCheckResult<Type> =
        //// TODO: assume sets is always correct since it's auto-generated
        TypeCheckResult.of(BasicType.UNIT)

    private fun visit(variable: ASTVariable, ass: Assumptions): TypeCheckResult<Type> {
        val scheme = ass.find(variable.name)
        val inst = tc.freshInstance(scheme)
        return TypeCheckResult.of(inst.value, inst.predicates)
    }

    private fun visit(constructor: ASTConstructor, ass: Assumptions): TypeCheckResult<Type> {
        val ctor = tc.findConstructor(constructor.name)

        val inst = tc.freshInstance(ctor.scheme)
        return TypeCheckResult.of(inst.value, inst.predicates)
    }

    private fun visit(astCase: ASTCase, ass: Assumptions): TypeCheckResult<Type> {
        val expResult = typeCheck(astCase.exp, ass)

        val typ = tc.newTVar()

        val result = TypeCheckResult.builder<Type>()

        for (val alt in astCase.alternatives) {
            val patternResult = tc.typeCheck(alt.pattern)

            tc.unify(expResult.value, patternResult.value)
            result.addPredicates(patternResult.predicates)

            val valueResult = tc.typeCheck(alt.value, patternResult.ass.join(ass))
            tc.unify(typ, valueResult.value)
            result.addPredicates(valueResult.predicates)
        }

        return result.build(typ)
    }
}
