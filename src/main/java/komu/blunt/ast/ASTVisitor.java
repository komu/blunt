package komu.blunt.ast;

public interface ASTVisitor<C,R> {
    R visit(ASTApplication application, C ctx);
    R visit(ASTConstant constant, C ctx);
    R visit(ASTIf ifExp, C ctx);
    R visit(ASTLambda lambda, C ctx);
    R visit(ASTLet let, C ctx);
    R visit(ASTLetRec letRec, C ctx);
    R visit(ASTSequence sequence, C ctx);
    R visit(ASTSet set, C ctx);
    R visit(ASTTuple tuple, C ctx);
    R visit(ASTVariable variable, C ctx);
}
