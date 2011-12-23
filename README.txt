A simple compiler/interpreter.

The main flow:

       String
         |
         |  reader.LispTokenizer
         |
         v
       tokens
         |
         |  reader.LispReader
         |
         v
     object tree
         |
         |  ast.ASTBuilder
         |
         v
  ast.ASTExpression
         |
         |  ASTExpression.analyze
         |
         v
  core.CoreExpression
         |
         |  type check (types.TypeEnvironment)
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
  - parser
  - type check ast, not core
  - data type definitions
  - type aliases
