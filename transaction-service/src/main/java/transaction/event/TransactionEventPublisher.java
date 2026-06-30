package transaction.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import transaction.entity.TransactionRecord;
import transaction.entity.TransactionStatus;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    public static final String TOPIC = "transaction-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(TransactionRecord record) {
        String eventType = record.getStatus() == TransactionStatus.COMPLETED
                ? "TRANSACTION_COMPLETED" : "TRANSACTION_FAILED";
        TransactionEvent event = new TransactionEvent(
                UUID.randomUUID().toString(), eventType, record.getId(), record.getAmount(),
                record.getMerchantId(), record.getStatus().name(), Instant.now());
        try {
            String json = objectMapper.writeValueAsString(event);
            // 키를 transactionId로 둬서 같은 거래의 이벤트는 항상 같은 파티션으로 가 순서가 보장된다.
            kafkaTemplate.send(TOPIC, record.getId(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize transaction event", e);
        }
    }
}
