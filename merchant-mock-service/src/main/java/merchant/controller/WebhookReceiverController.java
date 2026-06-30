package merchant.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import merchant.dto.ReceivedEventResponse;
import merchant.entity.ReceivedEvent;
import merchant.repository.ReceivedEventRepository;
import merchant.service.WebhookVerifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookReceiverController {

    private final WebhookVerifier webhookVerifier;
    private final ReceivedEventRepository receivedEventRepository;

    @PostMapping("/webhook/receive")
    public ResponseEntity<String> receive(@RequestHeader("X-Signature") String signature,
                                           @RequestHeader("X-Event-Id") String eventId,
                                           @RequestBody String rawBody) {
        if (!webhookVerifier.isValid(rawBody, signature)) {
            log.warn("Rejected webhook with invalid signature. eventId={}", eventId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"reason\":\"INVALID_SIGNATURE\"}");
        }

        if (receivedEventRepository.existsById(eventId)) {
            // 이미 처리된 이벤트도 200을 줘야 발신측 재시도가 멈춘다.
            log.info("Duplicate webhook ignored. eventId={}", eventId);
            return ResponseEntity.ok("{\"status\":\"DUPLICATE_IGNORED\"}");
        }

        receivedEventRepository.save(new ReceivedEvent(eventId, rawBody));
        log.info("Webhook accepted. eventId={}", eventId);
        return ResponseEntity.ok("{\"status\":\"RECEIVED\"}");
    }

    @GetMapping("/api/received-events")
    public List<ReceivedEventResponse> list() {
        return receivedEventRepository.findAll().stream().map(ReceivedEventResponse::from).toList();
    }
}
