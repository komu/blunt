package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Token {
    
    public final TokenType type;
    public final Object value;

    public Token(TokenType type) {
        this(type, null);
    }

    public Token(TokenType type, Object value) {
        this.type = checkNotNull(type);
        this.value = value;
    }

    public <T> T value(Class<T> cl) {
        return cl.cast(value);
    }
}
