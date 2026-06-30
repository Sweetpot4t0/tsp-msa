package tokenization.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tokenization.dto.ErrorResponse;

@RestControllerAdvice
public class TokenExceptionHandler {

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TokenNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("TOKEN_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(TokenNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleNotActive(TokenNotActiveException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("TOKEN_NOT_ACTIVE", e.getMessage()));
    }
}
