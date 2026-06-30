package transaction.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import transaction.client.dto.DetokenizeRequestDto;
import transaction.client.dto.DetokenizeResponseDto;

@Component
public class TokenizationClient {

    private final RestClient restClient;

    public TokenizationClient(@Qualifier("tokenizationRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    // HttpClientErrorException(4xx)만 "예상된 비즈니스 결과"로 잡아서 변환한다.
    // 그 외(연결 실패, 5xx 등)는 그대로 던져서 컨트롤러가 멱등성 키 점유를 풀고 재시도를 허용하게 한다.
    public ClientCallResult<DetokenizeResponseDto> detokenize(String token) {
        try {
            DetokenizeResponseDto response = restClient.post()
                    .uri("/internal/tokens/detokenize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DetokenizeRequestDto(token))
                    .retrieve()
                    .body(DetokenizeResponseDto.class);
            return ClientCallResult.success(response);
        } catch (HttpClientErrorException.NotFound e) {
            return ClientCallResult.failure("TOKEN_NOT_FOUND");
        } catch (HttpClientErrorException.Conflict e) {
            return ClientCallResult.failure("TOKEN_NOT_ACTIVE");
        }
    }
}
