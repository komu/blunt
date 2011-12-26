A simple compiler/interpreter.

The main flow:

       String
         |
         |  parser.Lexer
         |
         v
       tokens
         |
         |  parser.Parser
         |
         v
  ast.ASTExpression
         |
         |  ASTExpression.typeCheck
         |
         v
  ast.ASTExpression
         |
         |  ASTExpression.analyze
         |
         v
  core.CoreExpression
         |
         |  compile (CoreExpression.assemble)
         |
         v
   asm.Instructions
         |
         |  run (asm.VM)
         o

Things to do:
  - data type definitions
  - type aliases
  - type classes
