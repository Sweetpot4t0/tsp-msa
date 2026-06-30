package merchant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "received_event")
@Getter
@NoArgsConstructor
public class ReceivedEvent {

    @Id
    private String eventId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant receivedAt;

    public ReceivedEvent(String eventId, String payload) {
        this.eventId = eventId;
        this.payload = payload;
        this.receivedAt = Instant.now();
    }
}
