package notification.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEventPayload(
        String eventId,
        String eventType,
        String transactionId,
        BigDecimal amount,
        String merchantId,
        String status,
        Instant timestamp
) {
}
