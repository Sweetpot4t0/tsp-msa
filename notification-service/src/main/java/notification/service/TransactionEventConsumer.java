package notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final WebhookDispatchService webhookDispatchService;

    @KafkaListener(topics = "transaction-events")
    public void onTransactionEvent(String payloadJson) {
        log.info("Received transaction event: {}", payloadJson);
        webhookDispatchService.handleIncomingEvent(payloadJson);
    }
}
