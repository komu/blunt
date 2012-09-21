package komu.blunt

import komu.blunt.analyzer.AnalyzationException
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTVariable
import komu.blunt.eval.Evaluator
import komu.blunt.objects.EvaluationException
import komu.blunt.objects.Symbol
import komu.blunt.parser.SyntaxException

object Main {

    fun repl(evaluator: Evaluator) {
        val prompt = Prompt()

        while (true) {
            try {
                val exp = prompt.readExpression(">>> ")

                if (isSymbol("exit", exp)) {
                    break;
                } else if (isSymbol("dump", exp)) {
                    evaluator.dump()
                } else {
                    val (value, typ) = evaluator.evaluateWithType(exp)
                    println("$value: $typ")
                }
            } catch (e: SyntaxException) {
                println(e)
            } catch (e: AnalyzationException) {
                println(e)
            } catch (e: EvaluationException) {
                println(e)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun isSymbol(name: String, exp: ASTExpression) =
        exp is ASTVariable && Symbol(name) == exp.name
}

fun main(args : Array<String>) {
    val evaluator = Evaluator()

    evaluator.loadResource("prelude.blunt")

    for (val arg in args)
        evaluator.loadResource(arg)

    Main.repl(evaluator)
}
