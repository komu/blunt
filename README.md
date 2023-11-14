A simple compiler/interpreter.

The main flow:

```mermaid
graph TD
    A[String] -->|parser.Lexer| B[parser.Token sequence]
    B -->|parser.Parser| C[ast.ASTExpression]
    C -->|types.checker.TypeChecker| D[ast.ASTExpression]
    D -->|analyzer.Analyzer| E[core.CoreExpression]
    E -->|CoreExpression.assemble| F[asm.Instructions]
    F -->|asm.VM.run| G[Output]
```

Things to do:

- non-generic type parameters for constructors
- pattern matching against data types
    - rewrite if to pattern match
- explicit type definitions
- type aliases
- type classes
- newtype declarations
- type declarations for stdlib
