package komu.blunt.types.patterns;

public interface PatternVisitor<C,R> {
    R visit(ConstructorPattern pattern, C ctx);
    R visit(LiteralPattern pattern, C ctx);
    R visit(VariablePattern pattern, C ctx);
}
