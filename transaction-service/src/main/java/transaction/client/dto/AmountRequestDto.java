package transaction.client.dto;

import java.math.BigDecimal;

public record AmountRequestDto(BigDecimal amount, String referenceId) {
}
