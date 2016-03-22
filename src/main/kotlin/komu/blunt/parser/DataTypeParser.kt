package komu.blunt.parser

import komu.blunt.ast.AST
import komu.blunt.ast.ASTDataDefinition
import komu.blunt.parser.TokenType.Companion.ASSIGN
import komu.blunt.parser.TokenType.Companion.COMMA
import komu.blunt.parser.TokenType.Companion.DATA
import komu.blunt.parser.TokenType.Companion.DERIVING
import komu.blunt.parser.TokenType.Companion.END
import komu.blunt.parser.TokenType.Companion.OR
import komu.blunt.parser.TokenType.Companion.TYPE_OR_CTOR_NAME
import komu.blunt.types.*
import java.util.*

internal class DataTypeParser(val lexer: Lexer, val typeParser: TypeParser) {

    // data <type> <var>* = <constructor>+
    fun parseDataDefinition(): ASTDataDefinition  {
        lexer.expectIndentStartToken(DATA)

        val builder = DataTypeBuilder(lexer.readTokenValue(TYPE_OR_CTOR_NAME))

        while (!lexer.readMatchingToken(ASSIGN))
            builder.addVariable(typeParser.parseTypeVariable())

        do {
            parseConstructor(builder)
        } while (lexer.readMatchingToken(OR))

        if (lexer.readMatchingToken(DERIVING)) {
            lexer.inParens {
                do {
                    builder.addAutomaticallyDerivedClass(lexer.readTokenValue(TYPE_OR_CTOR_NAME))
                } while (lexer.readMatchingToken(COMMA))
            }
        }

        lexer.expectToken(END)

        return builder.build()
    }

    private fun parseConstructor(builder: DataTypeBuilder): Unit {
        val constructorName = lexer.readTokenValue(TYPE_OR_CTOR_NAME)

        val args = ArrayList<Type>()
        while (!lexer.nextTokenIs(OR) && !lexer.nextTokenIs(END) && !lexer.nextTokenIs(DERIVING))
            args.add(typeParser.parseTypePrimitive())

        builder.addConstructor(constructorName, args)
    }

    private class DataTypeBuilder(val typeName: String) {

        private val vars = ArrayList<TypeVariable>()
        private val constructors = ArrayList<ConstructorDefinition>()
        private val derivedClasses = ArrayList<String>()
        private var constructorIndex = 0

        fun addVariable(variable: TypeVariable) {
            vars.add(variable)
        }

        fun addConstructor(constructorName: String, args: List<Type>) {
            val scheme = Qualified.simple(functionType(args, getType())).quantify(vars)
            constructors.add(ConstructorDefinition(constructorIndex++, constructorName, scheme, args.size))
        }

        private fun getType() =
            genericType(typeName, vars)

        fun addAutomaticallyDerivedClass(className: String) {
            derivedClasses.add(className)
        }

        fun build(): ASTDataDefinition =
            AST.data(typeName, getType(), constructors, derivedClasses)
    }
}
