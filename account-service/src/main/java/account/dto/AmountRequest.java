package account.dto;

import java.math.BigDecimal;

public record AmountRequest(BigDecimal amount, String referenceId) {
}
