package notification.dto;

import notification.entity.WebhookDelivery;

import java.time.Instant;

public record WebhookDeliveryResponse(
        Long id,
        String eventId,
        String merchantId,
        String targetUrl,
        String status,
        int attemptCount,
        String lastError,
        Instant nextRetryAt
) {
    public static WebhookDeliveryResponse from(WebhookDelivery d) {
        return new WebhookDeliveryResponse(d.getId(), d.getEventId(), d.getMerchantId(), d.getTargetUrl(),
                d.getStatus().name(), d.getAttemptCount(), d.getLastError(), d.getNextRetryAt());
    }
}
