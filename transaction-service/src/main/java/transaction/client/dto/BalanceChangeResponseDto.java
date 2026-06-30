package transaction.client.dto;

import java.math.BigDecimal;

public record BalanceChangeResponseDto(BigDecimal newBalance) {
}
