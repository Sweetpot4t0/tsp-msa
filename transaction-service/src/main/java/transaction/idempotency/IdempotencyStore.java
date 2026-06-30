package transaction.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IdempotencyStore {

    private static final Duration TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "idem:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // SETNX(=setIfAbsent) + TTL이 한 번의 원자적 연산으로 나가기 때문에,
    // 동시에 같은 키로 들어온 두 요청 중 정확히 하나만 선점에 성공한다.
    public ClaimResult claim(String idempotencyKey) {
        String redisKey = redisKey(idempotencyKey);
        boolean acquired = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(redisKey, write(IdempotencyRecord.inProgress()), TTL));
        if (acquired) {
            return ClaimResult.acquired();
        }
        String existingJson = redisTemplate.opsForValue().get(redisKey);
        return ClaimResult.existing(read(existingJson));
    }

    public void complete(String idempotencyKey, int httpStatus, String bodyJson) {
        redisTemplate.opsForValue().set(redisKey(idempotencyKey),
                write(IdempotencyRecord.completed(httpStatus, bodyJson)), TTL);
    }

    // 처리 중 예상치 못한(인프라성) 오류가 나면 선점을 풀어줘서 클라이언트가 안전하게 재시도할 수 있게 한다.
    public void release(String idempotencyKey) {
        redisTemplate.delete(redisKey(idempotencyKey));
    }

    private String redisKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize idempotency record", e);
        }
    }

    private IdempotencyRecord read(String json) {
        try {
            return objectMapper.readValue(json, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize idempotency record", e);
        }
    }
}
