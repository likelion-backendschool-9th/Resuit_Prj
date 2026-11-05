package com.newlecture.resuit.controller;

import com.newlecture.resuit.config.SocialClientConfiguration;
import com.newlecture.resuit.dto.SessionMemberResponse;
import com.newlecture.resuit.service.AuthService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final SocialClientConfiguration.Clients clients;
    public record Provider(String id, boolean enabled, String loginUrl) {}
    public record Session(boolean authenticated, SessionMemberResponse user, List<Provider> providers, String csrfHeader, String csrfToken) {}

    @GetMapping("/session")
    public ResponseEntity<Session> session(Principal principal, CsrfToken csrf) {
        var member = principal == null ? null : authService.findSessionMember(principal.getName()).orElse(null);
        var providers = List.of("google", "github", "naver").stream().map(provider ->
            new Provider(provider, clients.providers().contains(provider), "/api/oauth2/authorization/" + provider)).toList();
        // local 프로필처럼 CSRF를 끈 경우에는 토큰이 없다.
        String csrfHeader = csrf == null ? null : csrf.getHeaderName();
        String csrfToken = csrf == null ? null : csrf.getToken();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Session(member != null, member, providers, csrfHeader, csrfToken));
    }
}
