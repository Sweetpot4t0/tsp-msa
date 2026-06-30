package transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import transaction.client.AccountClient;
import transaction.client.ClientCallResult;
import transaction.client.TokenizationClient;
import transaction.client.dto.BalanceChangeResponseDto;
import transaction.client.dto.DetokenizeResponseDto;
import transaction.dto.PaymentRequest;
import transaction.dto.TransactionResponse;
import transaction.entity.TransactionRecord;
import transaction.entity.TransactionStatus;
import transaction.event.TransactionEventPublisher;
import transaction.repository.TransactionRecordRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TokenizationClient tokenizationClient;
    private final AccountClient accountClient;
    private final TransactionRecordRepository transactionRecordRepository;
    private final TransactionEventPublisher eventPublisher;

    // 결제 거절(잔액 부족, 토큰 비활성 등)은 HTTP 에러가 아니라 비즈니스 결과다.
    // 그래서 이 메서드가 던지는 예외는 "예상 못한 인프라 오류"뿐이고, 그 외엔 항상 200 + 바디로 결과를 표현한다.
    @Transactional
    public TransactionOutcome process(String idempotencyKey, PaymentRequest request) {
        String transactionId = UUID.randomUUID().toString();

        ClientCallResult<DetokenizeResponseDto> detokenizeResult = tokenizationClient.detokenize(request.token());
        if (!detokenizeResult.success()) {
            return decline(transactionId, idempotencyKey, request, null, detokenizeResult.failureReason());
        }

        Long accountId = detokenizeResult.value().accountId();

        ClientCallResult<BalanceChangeResponseDto> debitResult =
                accountClient.debit(accountId, request.amount(), transactionId);
        if (!debitResult.success()) {
            return decline(transactionId, idempotencyKey, request, accountId, debitResult.failureReason());
        }

        TransactionRecord saved = transactionRecordRepository.save(new TransactionRecord(
                transactionId, idempotencyKey, request.token(), request.merchantId(), request.amount(),
                accountId, TransactionStatus.COMPLETED, null));
        // DB 커밋과 Kafka 발행은 원자적이지 않다 - 발행 직전에 죽으면 이벤트가 영구 누락된다(아웃박스 패턴으로 닫을 수 있는 갭).
        eventPublisher.publish(saved);

        log.info("Transaction completed. transactionId={}, accountId={}, amount={}",
                transactionId, accountId, request.amount());

        TransactionResponse body = new TransactionResponse(
                transactionId, TransactionStatus.COMPLETED.name(), request.amount(),
                debitResult.value().newBalance(), null);
        return new TransactionOutcome(200, body);
    }

    private TransactionOutcome decline(String transactionId, String idempotencyKey, PaymentRequest request,
                                        Long accountId, String reason) {
        TransactionRecord saved = transactionRecordRepository.save(new TransactionRecord(
                transactionId, idempotencyKey, request.token(), request.merchantId(), request.amount(),
                accountId, TransactionStatus.FAILED, reason));
        eventPublisher.publish(saved);

        log.info("Transaction declined. transactionId={}, reason={}", transactionId, reason);

        TransactionResponse body = new TransactionResponse(
                transactionId, TransactionStatus.FAILED.name(), request.amount(), null, reason);
        return new TransactionOutcome(200, body);
    }
}
