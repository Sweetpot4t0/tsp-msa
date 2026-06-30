package notification.dto;

public record RegisterMerchantRequest(String merchantId, String webhookUrl, String webhookSecret) {
}
