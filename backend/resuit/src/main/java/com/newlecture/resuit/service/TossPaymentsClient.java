package com.newlecture.resuit.service;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

// 승인 API: POST {api-base-url}/v1/payments/confirm, 인증은 "Basic base64(시크릿키:)"이다.
@Component
public class TossPaymentsClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};

    private final String clientKey;
    // 서버에서만 쓰는 비밀 키(test_sk_...). 로그나 응답에 절대 내보내지 않는다.
    private final String secretKey;
    private final RestClient restClient;

    public TossPaymentsClient(
            @Value("${resuit.toss.client-key:}") String clientKey,
            @Value("${resuit.toss.secret-key:}") String secretKey,
            @Value("${resuit.toss.api-base-url:https://api.tosspayments.com}") String apiBaseUrl) {
        this.clientKey = clientKey;
        this.secretKey = secretKey;
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(30));
        // 본문을 메모리에 모았다가 Content-Length와 함께 보낸다. 기본은 길이 없이 조각(chunked)으로 보낸다.
        this.restClient = RestClient.builder().baseUrl(apiBaseUrl)
                .requestFactory(new BufferingClientHttpRequestFactory(factory)).build();
    }

    public String clientKey() {
        return clientKey;
    }

    public boolean isConfigured() {
        return !clientKey.isBlank() && !secretKey.isBlank();
    }

    // 네트워크 오류나 토스 서버 오류(5xx)는 결과를 알 수 없으므로 502 예외로 끝낸다(결제는 PENDING으로 남는다).
    public ConfirmResult confirm(String paymentKey, String orderId, int amount) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        try {
            return restClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Authorization", auth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount))
                    .exchange((request, response) -> {
                        Map<String, Object> body = response.bodyTo(MAP_TYPE);
                        int code = response.getStatusCode().value();
                        // 5xx는 토스 쪽 문제라 승인 여부를 알 수 없다.
                        if (code >= 500) {
                            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "결제사 응답이 올바르지 않습니다. 잠시 후 다시 시도해 주세요.");
                        }
                        if (code < 300 && body != null) {
                            Object total = body.get("totalAmount");
                            return new ConfirmResult(true, String.valueOf(body.get("status")),
                                    total instanceof Number n ? n.intValue() : null, null, null);
                        }
                        String errorCode = body == null ? "UNKNOWN" : String.valueOf(body.get("code"));
                        String message = body == null ? "결제 승인에 실패했습니다." : String.valueOf(body.get("message"));
                        return new ConfirmResult(false, null, null, errorCode, message);
                    });
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "결제사에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.", e);
        }
    }

    public record ConfirmResult(boolean approved, String status, Integer totalAmount, String errorCode, String errorMessage) {
    }
}
