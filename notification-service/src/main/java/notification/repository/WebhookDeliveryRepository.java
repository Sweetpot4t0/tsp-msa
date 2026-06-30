package notification.repository;

import notification.entity.DeliveryStatus;
import notification.entity.WebhookDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> {
    List<WebhookDelivery> findByStatusAndNextRetryAtLessThanEqual(DeliveryStatus status, Instant now);
}
