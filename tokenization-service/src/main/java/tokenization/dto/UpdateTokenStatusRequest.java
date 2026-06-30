package tokenization.dto;

import tokenization.entity.TokenStatus;

public record UpdateTokenStatusRequest(TokenStatus status) {
}
