package account.dto;

import java.math.BigDecimal;

public record CreateAccountRequest(String ownerName, BigDecimal initialBalance) {
}
