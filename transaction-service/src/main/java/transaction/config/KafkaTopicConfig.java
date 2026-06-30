package transaction.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import transaction.event.TransactionEventPublisher;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name(TransactionEventPublisher.TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
