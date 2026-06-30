package account.dto;

import java.math.BigDecimal;

public record BalanceChangeResponse(BigDecimal newBalance) {
}
