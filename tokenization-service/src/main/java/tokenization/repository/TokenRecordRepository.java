package tokenization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tokenization.entity.TokenRecord;

public interface TokenRecordRepository extends JpaRepository<TokenRecord, String> {
}
