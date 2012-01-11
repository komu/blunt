package komu.blunt.parser;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Token<T> {
    
    public final TokenType<T> type;
    public final T value;
    private final SourceLocation location;

    public static <T> Token<T> ofType(TokenType<T> type, SourceLocation location) {
        return new Token<>(type, location);
    }

    private Token(TokenType<T> type, SourceLocation location) {
        this(type, null, location);
    }

    public Token(TokenType<T> type, T value, SourceLocation location) {
        this.type = checkNotNull(type);
        this.value = value;
        this.location = checkNotNull(location);
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

    public SourceLocation getLocation() {
        return location;
    }
}
