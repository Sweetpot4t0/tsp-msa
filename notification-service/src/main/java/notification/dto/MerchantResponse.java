package notification.dto;

import notification.entity.Merchant;

public record MerchantResponse(String merchantId, String webhookUrl) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(merchant.getMerchantId(), merchant.getWebhookUrl());
    }
}
