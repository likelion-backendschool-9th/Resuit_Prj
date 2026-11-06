package com.newlecture.resuit.config;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class SocialClientConfiguration {
    public static class Clients implements ClientRegistrationRepository {
        private final java.util.Map<String, ClientRegistration> registrations;
        public Clients(java.util.List<ClientRegistration> clients) {
            registrations = clients.stream().collect(java.util.stream.Collectors.toMap(ClientRegistration::getRegistrationId, c -> c));
        }
        @Override public ClientRegistration findByRegistrationId(String id) { return registrations.get(id); }
        public boolean enabled() { return !registrations.isEmpty(); }
        public java.util.Set<String> providers() { return registrations.keySet(); }
    }
    @Bean
    public Clients socialClients(Environment env, @Value("${resuit.auth.public-base-url:http://localhost:5173}") String base) {
        var clients = new ArrayList<ClientRegistration>();
        for (String provider : java.util.List.of("google", "github", "naver")) {
            String id = env.getProperty(provider.toUpperCase() + "_CLIENT_ID", "");
            String secret = env.getProperty(provider.toUpperCase() + "_CLIENT_SECRET", "");
            if (id.isBlank() || secret.isBlank()) continue;
            ClientRegistration.Builder builder;
            if (provider.equals("google")) builder = CommonOAuth2Provider.GOOGLE.getBuilder(provider).scope("profile", "email");
            else if (provider.equals("github")) builder = CommonOAuth2Provider.GITHUB.getBuilder(provider).scope("read:user");
            else builder = ClientRegistration.withRegistrationId(provider)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me").userNameAttributeName("response").clientName("Naver")
                // Spring Security 7은 PKCE 기본 ON. 네이버는 PKCE 미지원이라 끄지 않으면 서비스 오류 화면이 난다.
                .clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(false).build());
            clients.add(builder.clientId(id).clientSecret(secret)
                .redirectUri(base.replaceAll("/+$", "") + "/api/login/oauth2/code/" + provider).build());
        }
        return new Clients(clients);
    }
}
