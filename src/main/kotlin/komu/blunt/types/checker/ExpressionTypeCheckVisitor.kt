package komu.blunt.types.checker

import komu.blunt.ast.ASTExpression
import komu.blunt.ast.BindGroup
import komu.blunt.ast.ExplicitBinding
import komu.blunt.types.BasicType
import komu.blunt.types.Predicate
import komu.blunt.types.Type
import komu.blunt.types.typeFromObject
import komu.blunt.utils.init
import java.util.*

class ExpressionTypeCheckVisitor(private val tc: TypeChecker) {

    fun typeCheck(exp: ASTExpression, ctx: Assumptions): TypeCheckResult<Type> = when (exp) {
        is ASTExpression.Application -> typeCheckApplication(exp, ctx)
        is ASTExpression.Case        -> typeCheckCase(exp, ctx)
        is ASTExpression.Constant    -> typeCheckConstant(exp)
        is ASTExpression.Constructor -> typeCheckConstructor(exp)
        is ASTExpression.Lambda      -> typeCheckLambda(exp, ctx)
        is ASTExpression.Let         -> typeCheckLet(exp, ctx)
        is ASTExpression.LetRec      -> typeCheckLetRec(exp, ctx)
        is ASTExpression.Sequence    -> typeCheckSequence(exp, ctx)
        is ASTExpression.Set         -> typeCheckSet(exp, ctx)
        is ASTExpression.Variable    -> typeCheckVariable(exp, ctx)
    }

    private fun typeCheckApplication(application: ASTExpression.Application, ass: Assumptions): TypeCheckResult<Type> {
        val type = tc.newTVar()
        val tf = typeCheck(application.func, ass)
        val ta = typeCheck(application.arg, ass);

        tc.unify(tf.value, Type.function(ta.value, type))

        return TypeCheckResult(type, tf.predicates + ta.predicates)
    }

    private fun typeCheckConstant(constant: ASTExpression.Constant): TypeCheckResult<Type> =
        TypeCheckResult(typeFromObject(constant.value))

    private fun typeCheckLambda(lambda: ASTExpression.Lambda, ass: Assumptions): TypeCheckResult<Type> {
        val argumentType = tc.newTVar()
        val (resultType, predicates) = typeCheck(lambda.body, ass.augment(lambda.argument, argumentType.toScheme()))

        return TypeCheckResult(Type.function(argumentType, resultType), predicates)
    }

    private fun typeCheckLet(let: ASTExpression.Let, ass: Assumptions): TypeCheckResult<Type> {
        val (arg, exp) = let.bindings.singleOrNull() ?: throw UnsupportedOperationException("multi-var let is not supported")

        val expResult = typeCheck(exp, ass)

        val bodyResult = typeCheck(let.body, ass.augment(arg, expResult.value.toScheme()))

        return TypeCheckResult(bodyResult.value, expResult.predicates + bodyResult.predicates)
    }

    private fun typeCheckLetRec(letRec: ASTExpression.LetRec, ass: Assumptions): TypeCheckResult<Type> {
        val bindGroup = BindGroup(ArrayList<ExplicitBinding>(), letRec.bindings)

        val rs = tc.typeCheckBindGroup(bindGroup, ass)

        return typeCheck(letRec.body, ass + rs.value)
    }

    private fun typeCheckSequence(sequence: ASTExpression.Sequence, ass: Assumptions): TypeCheckResult<Type> {
        val predicates = sequence.exps.init.flatMap { typeCheck(it, ass).predicates }

        return typeCheck(sequence.exps.last(), ass) + predicates
    }

    private fun typeCheckSet(set: ASTExpression.Set, ass: Assumptions): TypeCheckResult<Type> =
        //// TODO: assume sets is always correct since it's auto-generated
        TypeCheckResult(BasicType.UNIT)

    private fun typeCheckVariable(variable: ASTExpression.Variable, ass: Assumptions): TypeCheckResult<Type> {
        val scheme = ass[variable.name]
        val inst = tc.freshInstance(scheme)
        return TypeCheckResult(inst.value, inst.predicates)
    }

    private fun typeCheckConstructor(constructor: ASTExpression.Constructor): TypeCheckResult<Type> {
        val ctor = tc.findConstructor(constructor.name)
        val inst = tc.freshInstance(ctor.scheme)
        return TypeCheckResult(inst.value, inst.predicates)
    }

    private fun typeCheckCase(case: ASTExpression.Case, ass: Assumptions): TypeCheckResult<Type> {
        val expResult = typeCheck(case.exp, ass)

        val type = tc.newTVar()
        val predicates = ArrayList<Predicate>()

        for (alt in case.alternatives) {
            val patternResult = tc.typeCheck(alt.pattern)

            tc.unify(expResult.value, patternResult.value)
            predicates += patternResult.predicates

            val valueResult = tc.typeCheck(alt.value, patternResult.ass + ass)
            tc.unify(type, valueResult.value)
            predicates += valueResult.predicates
        }

        return TypeCheckResult(type, predicates)
    }
}
