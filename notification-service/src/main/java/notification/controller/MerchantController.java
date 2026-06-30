package notification.controller;

import lombok.RequiredArgsConstructor;
import notification.dto.MerchantResponse;
import notification.dto.RegisterMerchantRequest;
import notification.entity.Merchant;
import notification.repository.MerchantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantRepository merchantRepository;

    @PostMapping("/api/merchants")
    public ResponseEntity<MerchantResponse> register(@RequestBody RegisterMerchantRequest request) {
        Merchant merchant = merchantRepository.save(
                new Merchant(request.merchantId(), request.webhookUrl(), request.webhookSecret()));
        return ResponseEntity.status(HttpStatus.CREATED).body(MerchantResponse.from(merchant));
    }
}
