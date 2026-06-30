package notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "webhook_delivery")
@Getter
@NoArgsConstructor
public class WebhookDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String merchantId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private int attemptCount;

    private Instant nextRetryAt;

    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public WebhookDelivery(String eventId, String merchantId, String payload, String targetUrl) {
        this.eventId = eventId;
        this.merchantId = merchantId;
        this.payload = payload;
        this.targetUrl = targetUrl;
        this.status = DeliveryStatus.PENDING;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void recordSuccess() {
        this.status = DeliveryStatus.SUCCESS;
        this.attemptCount += 1;
        this.lastError = null;
        this.nextRetryAt = null;
        this.updatedAt = Instant.now();
    }

    public void recordFailure(String error, Instant nextRetryAt) {
        this.status = DeliveryStatus.PENDING;
        this.attemptCount += 1;
        this.lastError = error;
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = Instant.now();
    }

    public void markPermanentlyFailed(String error) {
        this.status = DeliveryStatus.FAILED;
        this.attemptCount += 1;
        this.lastError = error;
        this.nextRetryAt = null;
        this.updatedAt = Instant.now();
    }
}
