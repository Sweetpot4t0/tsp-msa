package tokenization.dto;

public record IssueTokenRequest(Long accountId, String pan, String deviceId) {
}
