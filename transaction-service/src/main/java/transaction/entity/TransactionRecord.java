package transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transaction_record")
@Getter
@NoArgsConstructor
public class TransactionRecord {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String merchantId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    public TransactionRecord(String id, String idempotencyKey, String token, String merchantId, BigDecimal amount,
                              Long accountId, TransactionStatus status, String failureReason) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.token = token;
        this.merchantId = merchantId;
        this.amount = amount;
        this.accountId = accountId;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = Instant.now();
    }
}
