package merchant.repository;

import merchant.entity.ReceivedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivedEventRepository extends JpaRepository<ReceivedEvent, String> {
}
