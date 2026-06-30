package tokenization.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tokenization.dto.DetokenizeRequest;
import tokenization.dto.DetokenizeResponse;
import tokenization.dto.IssueTokenRequest;
import tokenization.dto.TokenResponse;
import tokenization.dto.UpdateTokenStatusRequest;
import tokenization.entity.TokenRecord;
import tokenization.service.TokenizationService;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenizationService tokenizationService;

    @PostMapping("/api/tokens/issue")
    public ResponseEntity<TokenResponse> issue(@RequestBody IssueTokenRequest request) {
        TokenRecord record = tokenizationService.issue(request.accountId(), request.pan(), request.deviceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(TokenResponse.from(record));
    }

    @PostMapping("/internal/tokens/detokenize")
    public DetokenizeResponse detokenize(@RequestBody DetokenizeRequest request) {
        return DetokenizeResponse.from(tokenizationService.detokenize(request.token()));
    }

    @PatchMapping("/api/tokens/{token}/status")
    public TokenResponse updateStatus(@PathVariable String token, @RequestBody UpdateTokenStatusRequest request) {
        return TokenResponse.from(tokenizationService.updateStatus(token, request.status()));
    }
}
