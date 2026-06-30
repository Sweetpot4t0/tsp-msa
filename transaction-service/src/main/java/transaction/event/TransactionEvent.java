package transaction.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
        String eventId,
        String eventType,
        String transactionId,
        BigDecimal amount,
        String merchantId,
        String status,
        Instant timestamp
) {
}
