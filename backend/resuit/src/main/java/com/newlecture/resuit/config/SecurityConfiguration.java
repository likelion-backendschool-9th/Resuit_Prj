package com.newlecture.resuit.config;

import com.newlecture.resuit.service.SocialMemberService;
import java.util.LinkedHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain security(HttpSecurity http, SocialClientConfiguration.Clients clients, SocialMemberService members, LoginRedirectHandler redirects) throws Exception {
        http.formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/session", "/api/oauth2/**", "/api/login/**", "/error").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**", "/api/v1/jobs/**", "/api/v1/posts/**").permitAll()
            .anyRequest().authenticated());
        // Keep session CSRF protection. The session endpoint exposes a masked token for JS requests.
        http.exceptionHandling(errors -> errors
            .authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"code\":\"UNAUTHENTICATED\",\"message\":\"로그인이 필요합니다.\"}");
            }).accessDeniedHandler((req, res, ex) -> {
                var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                boolean anonymous = authentication == null || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken;
                res.setStatus(anonymous ? 401 : 403); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write(anonymous ? "{\"code\":\"UNAUTHENTICATED\",\"message\":\"로그인이 필요합니다.\"}"
                    : "{\"code\":\"FORBIDDEN\",\"message\":\"요청 권한 또는 보안 토큰을 확인해 주세요.\"}");
            }));
        http.logout(logout -> logout.logoutUrl("/api/v1/auth/logout").deleteCookies("JSESSIONID")
            .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)));
        if (clients.enabled()) {
            var delegate = new DefaultOAuth2UserService();
            http.oauth2Login(oauth -> oauth
                .authorizationEndpoint(endpoint -> endpoint.baseUri("/api/oauth2/authorization"))
                .redirectionEndpoint(endpoint -> endpoint.baseUri("/api/login/oauth2/code/*"))
                .userInfoEndpoint(info -> info.userService(request -> {
                    var external = delegate.loadUser(request);
                    String provider = request.getClientRegistration().getRegistrationId();
                    String username = members.resolve(provider, external.getAttributes());
                    var attrs = new LinkedHashMap<String, Object>(external.getAttributes());
                    attrs.put("resuit_username", username); attrs.put("resuit_provider", provider);
                    return new DefaultOAuth2User(external.getAuthorities(), attrs, "resuit_username");
                }))
                .successHandler(redirects)
                .failureHandler(redirects));
        }
        return http.build();
    }
}
