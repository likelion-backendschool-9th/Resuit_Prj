package com.newlecture.resuit.controller;

import com.newlecture.resuit.config.OAuthLinkSession;
import com.newlecture.resuit.config.SocialClientConfiguration;
import com.newlecture.resuit.dto.SessionMemberResponse;
import com.newlecture.resuit.entity.SocialAccount;
import com.newlecture.resuit.repository.SocialAccountRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Set<String> PROVIDERS = Set.of("google", "github", "naver");

    private final AuthService authService;
    private final UserRepository users;
    private final SocialAccountRepository socialAccounts;
    private final SocialClientConfiguration.Clients clients;

    @Value("${resuit.integration.token-secret:}")
    private String linkSecret;

    public record Provider(String id, boolean enabled, String loginUrl) {}
    public record Session(boolean authenticated, SessionMemberResponse user, List<Provider> providers,
            List<String> linkedProviders, String csrfHeader, String csrfToken) {}
    public record LinkStartResponse(String authorizationUrl) {}

    @GetMapping("/session")
    public ResponseEntity<Session> session(Principal principal, CsrfToken csrf) {
        var member = principal == null ? null : authService.findSessionMember(principal.getName()).orElse(null);
        var providers = List.of("google", "github", "naver").stream().map(provider ->
            new Provider(provider, clients.providers().contains(provider), loginUrl(provider))).toList();
        List<String> linked = List.of();
        if (member != null) {
            linked = socialAccounts.findByUser_Id(member.id()).stream()
                    .map(SocialAccount::getProvider)
                    .sorted()
                    .toList();
        }
        String csrfHeader = csrf == null ? null : csrf.getHeaderName();
        String csrfToken = csrf == null ? null : csrf.getToken();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(new Session(member != null, member, providers, linked, csrfHeader, csrfToken));
    }

    @PostMapping("/link/{provider}")
    public LinkStartResponse startLink(
            @PathVariable String provider,
            Principal principal,
            HttpServletRequest request,
            HttpServletResponse response) {
        String normalized = requireProvider(provider);
        if (!clients.providers().contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "설정되지 않은 로그인 제공자입니다.");
        }
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        if (socialAccounts.findByUser_IdAndProvider(user.getId(), normalized).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 연결된 제공자입니다.");
        }
        OAuthLinkSession.store(request, response, user.getUsername(), normalized, linkSecret);
        String token = URLEncoder.encode(
                OAuthLinkSession.issue(user.getUsername(), normalized, linkSecret), StandardCharsets.UTF_8);
        return new LinkStartResponse(loginUrl(normalized) + "?link=" + token);
    }

    private static String loginUrl(String provider) {
        return "/api/oauth2/authorization/" + provider;
    }

    @DeleteMapping("/link/{provider}")
    public ResponseEntity<Void> unlink(
            @PathVariable String provider,
            Principal principal) {
        String normalized = requireProvider(provider);
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var user = users.findByUsername(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
        long count = socialAccounts.countByUser_Id(user.getId());
        if (count <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "마지막 로그인 수단은 해제할 수 없습니다.");
        }
        SocialAccount account = socialAccounts.findByUser_IdAndProvider(user.getId(), normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "연결된 제공자가 없습니다."));
        socialAccounts.delete(account);
        return ResponseEntity.noContent().build();
    }

    private static String requireProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "제공자가 필요합니다.");
        }
        String normalized = provider.trim().toLowerCase(Locale.ROOT);
        if (!PROVIDERS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 제공자입니다.");
        }
        return normalized;
    }
}
