package notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchant")
@Getter
@NoArgsConstructor
public class Merchant {

    @Id
    private String merchantId;

    @Column(nullable = false)
    private String webhookUrl;

    @Column(nullable = false)
    private String webhookSecret;

    public Merchant(String merchantId, String webhookUrl, String webhookSecret) {
        this.merchantId = merchantId;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
    }
}
