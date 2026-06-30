package tokenization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token_record")
@Getter
@NoArgsConstructor
public class TokenRecord {

    @Id
    private String token;

    @Column(nullable = false)
    private String maskedPan;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    public TokenRecord(String token, String maskedPan, String deviceId, Long accountId) {
        this.token = token;
        this.maskedPan = maskedPan;
        this.deviceId = deviceId;
        this.accountId = accountId;
        this.status = TokenStatus.ACTIVE;
    }

    public void changeStatus(TokenStatus newStatus) {
        this.status = newStatus;
    }
}
