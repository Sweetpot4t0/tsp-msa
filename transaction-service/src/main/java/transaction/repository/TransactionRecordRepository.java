package transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transaction.entity.TransactionRecord;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, String> {
}
