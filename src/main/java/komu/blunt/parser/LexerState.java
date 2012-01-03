package komu.blunt.parser;

import java.util.List;

final class LexerState {
    
    final int position;
    final List<Integer> indents;
    final Token<?> nextToken;

    LexerState(int position, List<Integer> indents, Token<?> nextToken) {
        this.position = position;
        this.indents = indents;
        this.nextToken  = nextToken;
    }
}
