package com.newlecture.resuit.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Configuration
public class OAuthAuthorizationConfig {
    public static final String AUTHORIZATION_BASE_URI = "/api/oauth2/authorization";

    @Bean
    OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver(
            SocialClientConfiguration.Clients clients,
            @Value("${resuit.integration.token-secret:}") String linkSecret) {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(clients, AUTHORIZATION_BASE_URI);
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuthLinkSession.captureRequestToken(request, linkSecret);
                return withLinkTarget(withGithubAccountPrompt(delegate.resolve(request)), request, linkSecret);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuthLinkSession.captureRequestToken(request, linkSecret);
                return withLinkTarget(
                        withGithubAccountPrompt(delegate.resolve(request, clientRegistrationId)),
                        request,
                        linkSecret);
            }
        };
    }

    /**
     * GitHub 로그아웃 return_to 는 외부 콜백을 버리고 GitHub 홈에 남긴다.
     * 인가 주소에 prompt=select_account 를 붙여 계정 선택 후 등록된 Resuit 콜백으로 돌아온다.
     */
    static OAuth2AuthorizationRequest withGithubAccountPrompt(OAuth2AuthorizationRequest request) {
        if (request == null || request.getAuthorizationRequestUri() == null) {
            return request;
        }
        String uri = request.getAuthorizationRequestUri();
        if (!uri.startsWith("https://github.com/login/oauth/authorize") || uri.contains("prompt=")) {
            return request;
        }
        Map<String, Object> additional = new LinkedHashMap<>();
        if (request.getAdditionalParameters() != null) {
            additional.putAll(request.getAdditionalParameters());
        }
        additional.put("prompt", "select_account");
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(additional)
                .authorizationRequestUri(uri + "&prompt=select_account")
                .build();
    }

    static OAuth2AuthorizationRequest withLinkTarget(
            OAuth2AuthorizationRequest request, HttpServletRequest http, String secret) {
        if (request == null) return null;
        OAuthLinkSession.Intent intent = OAuthLinkSession.parse(http.getParameter("link"), secret);
        if (intent == null) return request;
        return OAuth2AuthorizationRequest.from(request)
                .attributes(attributes -> {
                    attributes.put(OAuthLinkSession.USERNAME, intent.username());
                    attributes.put(OAuthLinkSession.PROVIDER, intent.provider());
                })
                .build();
    }

    @Bean
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> oauth2AuthorizationRequestRepository() {
        return new LinkingAuthorizationRequestRepository();
    }
}
