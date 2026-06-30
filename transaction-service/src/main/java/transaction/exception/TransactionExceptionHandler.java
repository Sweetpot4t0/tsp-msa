package transaction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import transaction.dto.ErrorBody;

@RestControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorBody> handleDownstreamFailure(RestClientException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorBody("DOWNSTREAM_UNAVAILABLE", e.getMessage()));
    }
}
