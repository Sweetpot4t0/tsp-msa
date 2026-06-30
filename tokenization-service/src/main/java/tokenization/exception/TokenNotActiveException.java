package tokenization.exception;

import tokenization.entity.TokenStatus;

public class TokenNotActiveException extends RuntimeException {
    public TokenNotActiveException(String token, TokenStatus status) {
        super("Token is not active: " + token + " (status=" + status + ")");
    }
}
