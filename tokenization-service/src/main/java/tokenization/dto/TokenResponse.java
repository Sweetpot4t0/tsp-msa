package tokenization.dto;

import tokenization.entity.TokenRecord;
import tokenization.entity.TokenStatus;

public record TokenResponse(String token, String maskedPan, TokenStatus status) {
    public static TokenResponse from(TokenRecord record) {
        return new TokenResponse(record.getToken(), record.getMaskedPan(), record.getStatus());
    }
}
