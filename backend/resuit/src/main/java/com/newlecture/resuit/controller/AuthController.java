package com.newlecture.resuit.controller;

import com.newlecture.resuit.config.SocialClientConfiguration;
import com.newlecture.resuit.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository users;
    private final SocialClientConfiguration.Clients clients;
    public record Member(UUID id, String username, String nickname) {}
    public record Provider(String id, boolean enabled, String loginUrl) {}
    public record Session(boolean authenticated, Member user, List<Provider> providers, String csrfHeader, String csrfToken) {}

    @GetMapping("/session")
    public ResponseEntity<Session> session(Principal principal, CsrfToken csrf) {
        var member = principal == null ? null : users.findByUsername(principal.getName())
            .map(user -> new Member(user.getId(), user.getUsername(), user.getNickname())).orElse(null);
        var providers = List.of("google", "github", "naver").stream().map(provider ->
            new Provider(provider, clients.providers().contains(provider), "/api/oauth2/authorization/" + provider)).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Session(member != null, member, providers, csrf.getHeaderName(), csrf.getToken()));
    }
}
