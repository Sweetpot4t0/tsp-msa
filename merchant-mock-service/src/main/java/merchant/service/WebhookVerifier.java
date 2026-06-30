package merchant.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class WebhookVerifier {

    private static final String ALGORITHM = "HmacSHA256";

    private final String secret;

    public WebhookVerifier(@Value("${merchant.webhook-secret}") String secret) {
        this.secret = secret;
    }

    public boolean isValid(String payload, String signatureHex) {
        byte[] expected = sign(payload);
        byte[] provided;
        try {
            provided = HexFormat.of().parseHex(signatureHex);
        } catch (IllegalArgumentException e) {
            return false;
        }
        // 상수 시간 비교로 타이밍 공격을 막는다.
        return MessageDigest.isEqual(expected, provided);
    }

    private byte[] sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute webhook signature", e);
        }
    }
}
