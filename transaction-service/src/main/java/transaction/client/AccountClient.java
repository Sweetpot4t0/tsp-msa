package transaction.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import transaction.client.dto.AmountRequestDto;
import transaction.client.dto.BalanceChangeResponseDto;

import java.math.BigDecimal;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(@Qualifier("accountRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ClientCallResult<BalanceChangeResponseDto> debit(Long accountId, BigDecimal amount, String referenceId) {
        try {
            BalanceChangeResponseDto response = restClient.post()
                    .uri("/internal/accounts/{id}/debit", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AmountRequestDto(amount, referenceId))
                    .retrieve()
                    .body(BalanceChangeResponseDto.class);
            return ClientCallResult.success(response);
        } catch (HttpClientErrorException.NotFound e) {
            return ClientCallResult.failure("ACCOUNT_NOT_FOUND");
        } catch (HttpClientErrorException.Conflict e) {
            return ClientCallResult.failure("INSUFFICIENT_BALANCE");
        }
    }
}
