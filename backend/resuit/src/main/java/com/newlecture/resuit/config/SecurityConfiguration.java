package com.newlecture.resuit.config;

import com.newlecture.resuit.service.SocialMemberService;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain security(HttpSecurity http, SocialClientConfiguration.Clients clients,
            SocialMemberService members, LoginRedirectHandler redirects,
            OAuth2AuthorizationRequestResolver oauth2AuthorizationRequestResolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> oauth2AuthorizationRequestRepository,
            ObjectProvider<DevHeaderAuthenticationFilter> devHeaderAuth,
            @Value("${app.dev-header-auth.enabled:false}") boolean headerAuthEnabled) throws Exception {
        http.formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable);
        if (headerAuthEnabled) {
            // 로컬 프로필 API는 세션 없이 X-Username으로 호출하므로 CSRF를 끈다.
            http.csrf(AbstractHttpConfigurer::disable);
            DevHeaderAuthenticationFilter filter = devHeaderAuth.getIfAvailable();
            if (filter != null) {
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
            }
        }
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/jobs/me/**").authenticated()
            .requestMatchers("/api/v1/auth/session", "/api/oauth2/**", "/api/login/**", "/error").permitAll()
            .requestMatchers(HttpMethod.GET, "/media/image/**", "/media/video/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**", "/api/v1/jobs/**", "/api/v1/posts/**",
                    "/api/v1/users/*/tweets", "/api/v1/users/*/feedbacks",
                    "/api/v1/users/*/github/repos", "/api/v1/users/*/github/contributions",
                    "/api/v1/users/*/blog-posts").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/users/{username}").permitAll()
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
                .authorizationEndpoint(endpoint -> endpoint
                    .baseUri(OAuthAuthorizationConfig.AUTHORIZATION_BASE_URI)
                    .authorizationRequestResolver(oauth2AuthorizationRequestResolver)
                    .authorizationRequestRepository(oauth2AuthorizationRequestRepository))
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
