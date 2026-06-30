package transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import transaction.dto.ErrorBody;
import transaction.dto.PaymentRequest;
import transaction.idempotency.ClaimResult;
import transaction.idempotency.IdempotencyRecord;
import transaction.idempotency.IdempotencyStore;
import transaction.service.TransactionOutcome;
import transaction.service.TransactionService;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final IdempotencyStore idempotencyStore;
    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/transactions")
    public ResponseEntity<String> pay(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                       @RequestBody PaymentRequest request) {
        ClaimResult claim = idempotencyStore.claim(idempotencyKey);

        if (!claim.claimed()) {
            IdempotencyRecord existing = claim.existing();
            if (existing.isInProgress()) {
                return jsonResponse(HttpStatus.CONFLICT.value(),
                        toJson(new ErrorBody("REQUEST_IN_PROGRESS", "Duplicate request is still being processed")));
            }
            // 이전에 완료된 동일 키 요청 -> 재처리 없이 캐시된 응답을 그대로 재생
            return jsonResponse(existing.httpStatus(), existing.bodyJson());
        }

        try {
            TransactionOutcome outcome = transactionService.process(idempotencyKey, request);
            String bodyJson = toJson(outcome.body());
            idempotencyStore.complete(idempotencyKey, outcome.httpStatus(), bodyJson);
            return jsonResponse(outcome.httpStatus(), bodyJson);
        } catch (RuntimeException e) {
            idempotencyStore.release(idempotencyKey);
            throw e;
        }
    }

    private ResponseEntity<String> jsonResponse(int status, String json) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(json);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize response body", e);
        }
    }
}
