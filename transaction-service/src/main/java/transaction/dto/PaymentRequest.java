package transaction.dto;

import java.math.BigDecimal;

public record PaymentRequest(String token, String merchantId, BigDecimal amount) {
}
