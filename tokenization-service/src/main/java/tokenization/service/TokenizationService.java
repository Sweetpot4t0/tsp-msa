package tokenization.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokenization.entity.TokenRecord;
import tokenization.entity.TokenStatus;
import tokenization.exception.TokenNotActiveException;
import tokenization.exception.TokenNotFoundException;
import tokenization.repository.TokenRecordRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenizationService {

    private final TokenRecordRepository tokenRecordRepository;

    @Transactional
    public TokenRecord issue(Long accountId, String pan, String deviceId) {
        String token = "tok_" + UUID.randomUUID().toString().replace("-", "");
        // 실카드번호(pan)는 마스킹에만 쓰이고 그 외엔 어디에도 저장하지 않는다.
        String maskedPan = maskPan(pan);
        return tokenRecordRepository.save(new TokenRecord(token, maskedPan, deviceId, accountId));
    }

    public TokenRecord detokenize(String token) {
        TokenRecord record = findOrThrow(token);
        if (record.getStatus() != TokenStatus.ACTIVE) {
            throw new TokenNotActiveException(token, record.getStatus());
        }
        return record;
    }

    @Transactional
    public TokenRecord updateStatus(String token, TokenStatus newStatus) {
        TokenRecord record = findOrThrow(token);
        record.changeStatus(newStatus);
        return record;
    }

    private TokenRecord findOrThrow(String token) {
        return tokenRecordRepository.findById(token)
                .orElseThrow(() -> new TokenNotFoundException(token));
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            throw new IllegalArgumentException("Invalid PAN");
        }
        String last4 = pan.substring(pan.length() - 4);
        return "*".repeat(pan.length() - 4) + last4;
    }
}
