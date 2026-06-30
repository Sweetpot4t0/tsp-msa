package notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.dto.TransactionEventPayload;
import notification.entity.DeliveryStatus;
import notification.entity.Merchant;
import notification.entity.WebhookDelivery;
import notification.repository.MerchantRepository;
import notification.repository.WebhookDeliveryRepository;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatchService {

    private static final int MAX_ATTEMPTS = 6;

    private final ObjectMapper objectMapper;
    private final MerchantRepository merchantRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final WebhookSigner webhookSigner;
    private final RestClient restClient = RestClient.create();

    @Transactional
    public void handleIncomingEvent(String payloadJson) {
        TransactionEventPayload event = parse(payloadJson);
        Merchant merchant = merchantRepository.findById(event.merchantId()).orElse(null);
        if (merchant == null) {
            log.warn("No merchant registered for merchantId={}, dropping eventId={}", event.merchantId(), event.eventId());
            return;
        }

        WebhookDelivery delivery = webhookDeliveryRepository.save(
                new WebhookDelivery(event.eventId(), merchant.getMerchantId(), payloadJson, merchant.getWebhookUrl()));
        attemptDelivery(delivery, merchant.getWebhookSecret());
    }

    // 운영이라면 분 단위 간격이 맞겠지만, 학습/테스트에서 재시도를 눈으로 보기 위해 짧게 잡았다.
    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void retryDueDeliveries() {
        List<WebhookDelivery> due = webhookDeliveryRepository
                .findByStatusAndNextRetryAtLessThanEqual(DeliveryStatus.PENDING, Instant.now());
        for (WebhookDelivery delivery : due) {
            merchantRepository.findById(delivery.getMerchantId())
                    .ifPresent(merchant -> attemptDelivery(delivery, merchant.getWebhookSecret()));
        }
    }

    private void attemptDelivery(WebhookDelivery delivery, String secret) {
        String signature = webhookSigner.sign(delivery.getPayload(), secret);
        try {
            restClient.post()
                    .uri(delivery.getTargetUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Signature", signature)
                    .header("X-Event-Id", delivery.getEventId())
                    .body(delivery.getPayload())
                    .retrieve()
                    .toBodilessEntity();
            delivery.recordSuccess();
            log.info("Webhook delivered. eventId={}, merchantId={}", delivery.getEventId(), delivery.getMerchantId());
        } catch (RestClientException e) {
            handleFailure(delivery, e);
        }
        webhookDeliveryRepository.save(delivery);
    }

    private void handleFailure(WebhookDelivery delivery, RestClientException e) {
        int nextAttempt = delivery.getAttemptCount() + 1;
        if (nextAttempt >= MAX_ATTEMPTS) {
            delivery.markPermanentlyFailed(e.getMessage());
            log.error("Webhook permanently failed after {} attempts. eventId={}", nextAttempt, delivery.getEventId());
            return;
        }
        Duration backoff = Duration.ofMinutes((long) Math.pow(2, nextAttempt - 1));
        delivery.recordFailure(e.getMessage(), Instant.now().plus(backoff));
        log.warn("Webhook delivery failed, will retry at {}. eventId={}, attempt={}",
                delivery.getNextRetryAt(), delivery.getEventId(), nextAttempt);
    }

    private TransactionEventPayload parse(String json) {
        try {
            return objectMapper.readValue(json, TransactionEventPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse transaction event payload", e);
        }
    }
}
