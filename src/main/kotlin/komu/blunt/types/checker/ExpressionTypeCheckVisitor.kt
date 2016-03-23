package komu.blunt.types.checker

import komu.blunt.ast.ASTExpression
import komu.blunt.ast.BindGroup
import komu.blunt.ast.ExplicitBinding
import komu.blunt.types.BasicType
import komu.blunt.types.Predicate
import komu.blunt.types.Type
import komu.blunt.types.functionType
import komu.blunt.utils.init
import java.util.*

class ExpressionTypeCheckVisitor(private val tc: TypeChecker) {

    fun typeCheck(exp: ASTExpression, ctx: Assumptions): TypeCheckResult<Type> =
      when (exp) {
        is ASTExpression.Application -> visit(exp, ctx)
        is ASTExpression.Constant -> visit(exp)
        is ASTExpression.Lambda -> visit(exp, ctx)
        is ASTExpression.Let -> visit(exp, ctx)
        is ASTExpression.LetRec -> visit(exp, ctx)
        is ASTExpression.Sequence -> visit(exp, ctx)
        is ASTExpression.Set -> visit(exp, ctx)
        is ASTExpression.Variable -> visit(exp, ctx)
        is ASTExpression.Constructor -> visit(exp)
        is ASTExpression.Case -> visit(exp, ctx)
      }

    private fun visit(application: ASTExpression.Application, ass: Assumptions): TypeCheckResult<Type> {
        val te = typeCheck(application.func, ass)
        val tf = typeCheck(application.arg, ass);

        val t = tc.newTVar()

        tc.unify(functionType(tf.value, t), te.value)

        return TypeCheckResult.of(t, te.predicates, tf.predicates)
    }

    private fun visit(constant: ASTExpression.Constant): TypeCheckResult<Type> =
        TypeCheckResult.of(constant.valueType())

    private fun visit(lambda: ASTExpression.Lambda, ass: Assumptions): TypeCheckResult<Type> {
        val argumentType = tc.newTVar()
        val as2 = Assumptions.singleton(lambda.argument, argumentType.toScheme() )
        val (resultType, predicates) = typeCheck(lambda.body, ass + as2)

        return TypeCheckResult.of(functionType(argumentType, resultType), predicates)
    }

    private fun visit(let: ASTExpression.Let, ass: Assumptions): TypeCheckResult<Type> {
        if (let.bindings.size != 1)
            throw UnsupportedOperationException("multi-var let is not supported")

        val arg = let.bindings.first().name
        val exp = let.bindings.first().expr

        val expResult = typeCheck(exp, ass)

        val as2 = Assumptions.singleton(arg, expResult.value.toScheme())

        val bodyResult = typeCheck(let.body, ass + as2)

        return TypeCheckResult.of(bodyResult.value, expResult.predicates, bodyResult.predicates)
    }

    private fun visit(letRec: ASTExpression.LetRec, ass: Assumptions): TypeCheckResult<Type> {
        val bindGroup = BindGroup(ArrayList<ExplicitBinding>(), letRec.bindings)

        val rs = tc.typeCheckBindGroup(bindGroup, ass)

        return typeCheck(letRec.body, ass + rs.value)
    }

    private fun visit(sequence: ASTExpression.Sequence, ass: Assumptions): TypeCheckResult<Type> {
        val predicates = ArrayList<Predicate>()

        for (exp in sequence.exps.init)
            predicates.addAll(typeCheck(exp, ass).predicates)

        return typeCheck(sequence.exps.last(), ass).withAddedPredicates(predicates)
    }

    private fun visit(set: ASTExpression.Set, ass: Assumptions): TypeCheckResult<Type> =
        //// TODO: assume sets is always correct since it's auto-generated
        TypeCheckResult.of(BasicType.UNIT)

    private fun visit(variable: ASTExpression.Variable, ass: Assumptions): TypeCheckResult<Type> {
        val scheme = ass[variable.name]
        val inst = tc.freshInstance(scheme)
        return TypeCheckResult.of(inst.value, inst.predicates)
    }

    private fun visit(constructor: ASTExpression.Constructor): TypeCheckResult<Type> {
        val ctor = tc.findConstructor(constructor.name)

        val inst = tc.freshInstance(ctor.scheme)
        return TypeCheckResult.of(inst.value, inst.predicates)
    }

    private fun visit(case: ASTExpression.Case, ass: Assumptions): TypeCheckResult<Type> {
        val expResult = typeCheck(case.exp, ass)

        val typ = tc.newTVar()

        val result = TypeCheckResult.builder<Type>()

        for (alt in case.alternatives) {
            val patternResult = tc.typeCheck(alt.pattern)

            tc.unify(expResult.value, patternResult.value)
            result.addPredicates(patternResult.predicates)

            val valueResult = tc.typeCheck(alt.value, patternResult.ass + ass)
            tc.unify(typ, valueResult.value)
            result.addPredicates(valueResult.predicates)
        }

        return result.build(typ)
    }
}
