package komu.blunt

import java.io.BufferedReader
import java.io.InputStreamReader
import komu.blunt.ast.ASTExpression
import komu.blunt.parser.parseExpression

class Prompt {

    private val reader = BufferedReader(InputStreamReader(System.`in`))

    fun readExpression(prompt: String): ASTExpression {
        print(prompt)
        System.out.flush()

        return parseExpression(reader.readLine() ?: "")
    }
}
