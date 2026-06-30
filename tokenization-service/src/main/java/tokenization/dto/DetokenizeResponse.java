package tokenization.dto;

import tokenization.entity.TokenRecord;
import tokenization.entity.TokenStatus;

public record DetokenizeResponse(Long accountId, TokenStatus status) {
    public static DetokenizeResponse from(TokenRecord record) {
        return new DetokenizeResponse(record.getAccountId(), record.getStatus());
    }
}
