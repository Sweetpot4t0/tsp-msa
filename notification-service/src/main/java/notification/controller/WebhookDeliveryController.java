package notification.controller;

import lombok.RequiredArgsConstructor;
import notification.dto.WebhookDeliveryResponse;
import notification.repository.WebhookDeliveryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WebhookDeliveryController {

    private final WebhookDeliveryRepository webhookDeliveryRepository;

    @GetMapping("/api/webhook-deliveries")
    public List<WebhookDeliveryResponse> list() {
        return webhookDeliveryRepository.findAll().stream()
                .sorted(Comparator.comparing(notification.entity.WebhookDelivery::getId).reversed())
                .map(WebhookDeliveryResponse::from)
                .toList();
    }
}
