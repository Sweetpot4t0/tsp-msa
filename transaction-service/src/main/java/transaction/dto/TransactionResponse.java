package transaction.dto;

import java.math.BigDecimal;

public record TransactionResponse(
        String transactionId,
        String status,
        BigDecimal amount,
        BigDecimal newBalance,
        String failureReason
) {
}
