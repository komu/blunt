package komu.blunt

import komu.blunt.ast.ASTExpression
import komu.blunt.parser.Parser

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Prompt {

    private val reader = BufferedReader(InputStreamReader(System.`in`.sure()))

    fun readExpression(prompt: String): ASTExpression {
        print(prompt)
        System.out?.flush()

        return Parser.parseExpression(reader.readLine()).sure())
    }
}

