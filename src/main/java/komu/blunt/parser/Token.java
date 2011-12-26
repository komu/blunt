package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Token<T> {
    
    public final TokenType<T> type;
    public final T value;

    public static <T> Token<T> ofType(TokenType<T> type) {
        return new Token<T>(type);
    }

    private Token(TokenType<T> type) {
        this(type, null);
    }

    public Token(TokenType<T> type, T value) {
        this.type = checkNotNull(type);
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <U> Token<U> asType(TokenType<U> type) {
        if (this.type == type)
            return (Token<U>) this;
        else
            throw new IllegalArgumentException("can't cast " + this + " to " + type);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
