package komu.blunt

import komu.blunt.analyzer.AnalyzationException
import komu.blunt.ast.ASTExpression
import komu.blunt.ast.ASTVariable
import komu.blunt.eval.Evaluator
import komu.blunt.objects.EvaluationException
import komu.blunt.parser.SyntaxException

object Main {

    fun repl(evaluator: Evaluator) {
        val prompt = Prompt()

        loop@while (true) {
            try {
                val exp = prompt.readExpression(">>> ")
                when {
                    exp.isSymbol("exit") -> break@loop
                    exp.isSymbol("dump") -> evaluator.dump()
                    else                 -> evaluator.evaluateAndPrint(exp)
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

    fun Evaluator.evaluateAndPrint(exp: ASTExpression) {
        val (value, typ) = evaluateWithType(exp)
        println("$value: $typ")
    }

    fun ASTExpression.isSymbol(name: String) =
        this is ASTVariable && name == this.name.toString()
}

fun main(args : Array<String>) {
    val evaluator = Evaluator()

    evaluator.loadResource("prelude.blunt")

    for (arg in args)
        evaluator.loadResource(arg)

    Main.repl(evaluator)
}
