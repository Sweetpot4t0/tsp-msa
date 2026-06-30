package merchant.dto;

import merchant.entity.ReceivedEvent;

import java.time.Instant;

public record ReceivedEventResponse(String eventId, String payload, Instant receivedAt) {
    public static ReceivedEventResponse from(ReceivedEvent event) {
        return new ReceivedEventResponse(event.getEventId(), event.getPayload(), event.getReceivedAt());
    }
}
