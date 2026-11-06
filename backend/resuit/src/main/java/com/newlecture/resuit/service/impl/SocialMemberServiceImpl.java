package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.config.LinkingAuthorizationRequestRepository;
import com.newlecture.resuit.config.OAuthLinkSession;
import com.newlecture.resuit.entity.SocialAccount;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.SocialAccountRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.SocialMemberService;
import jakarta.persistence.EntityManager;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class SocialMemberServiceImpl implements SocialMemberService {
    private final UserRepository users;
    private final SocialAccountRepository accounts;
    private final EntityManager entityManager;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${resuit.integration.token-secret:}")
    private String linkSecret;

    @Override
    @Transactional
    public String resolve(String provider, Map<String, Object> attributes) {
        if (!java.util.Set.of("google", "github", "naver").contains(provider)) {
            throw new OAuth2AuthenticationException("unsupported_provider");
        }
        Map<?, ?> profile = provider.equals("naver") && attributes.get("response") instanceof Map<?, ?> response
            ? response : attributes;
        String subject = text(profile.get(provider.equals("google") ? "sub" : "id"));
        if (subject == null || subject.length() > 255) throw new OAuth2AuthenticationException("missing_subject");

        OAuthLinkSession.Intent linkIntent = currentLinkIntent();
        if (linkIntent != null) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || authentication instanceof AnonymousAuthenticationToken
                    || authentication.getName() == null
                    || !linkIntent.username().equals(authentication.getName())) {
                throw oauthError("link_user_missing", "연결할 로그인 세션을 확인할 수 없습니다. 다시 로그인해 주세요.");
            }
            if (!linkIntent.provider().equals(provider)) {
                throw oauthError("link_provider_mismatch", "연결하려는 제공자와 로그인 제공자가 다릅니다.");
            }
            return linkExistingUser(linkIntent.username(), provider, subject);
        }

        // PostgreSQL에서만 동시 첫 로그인 직렬화. H2(local)에는 해당 함수가 없다.
        if (datasourceUrl.contains("postgresql")) {
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(:key, 0))")
                .setParameter("key", provider + ":" + subject).getSingleResult();
        }

        String preferredUsername = preferredUsername(provider, profile);
        String nickname = preferredNickname(profile, preferredUsername);

        return accounts.findByProviderAndSubject(provider, subject).map(account -> {
            User user = account.getUser();
            upgradeGeneratedUsername(user, preferredUsername);
            return user.getUsername();
        }).orElseGet(() -> {
            var user = users.save(User.builder()
                    .username(allocateUsername(preferredUsername))
                    .nickname(nickname)
                    .role("USER")
                    .build());
            accounts.save(SocialAccount.builder().provider(provider).subject(subject).user(user).build());
            return user.getUsername();
        });
    }

    private String linkExistingUser(String username, String provider, String subject) {
        User current = users.findByUsername(username)
                .orElseThrow(() -> oauthError("link_user_missing", "연결할 로그인 세션 회원을 찾을 수 없습니다."));

        var sameProvider = accounts.findByUser_IdAndProvider(current.getId(), provider);
        if (sameProvider.isPresent()) {
            if (subject.equals(sameProvider.get().getSubject())) {
                return current.getUsername();
            }
            throw oauthError("provider_already_connected", "이미 이 제공자가 연결되어 있습니다.");
        }

        var existing = accounts.findByProviderAndSubject(provider, subject);
        if (existing.isPresent()) {
            SocialAccount account = existing.get();
            User owner = account.getUser();
            if (owner == null || !owner.getId().equals(current.getId())) {
                account.assignUser(current);
            }
            return current.getUsername();
        }

        if (datasourceUrl.contains("postgresql")) {
            entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(:key, 0))")
                .setParameter("key", "link:" + provider + ":" + subject).getSingleResult();
        }

        accounts.save(SocialAccount.builder().provider(provider).subject(subject).user(current).build());
        return current.getUsername();
    }

    private static OAuth2AuthenticationException oauthError(String code, String description) {
        return new OAuth2AuthenticationException(new OAuth2Error(code, description, null));
    }

    private OAuthLinkSession.Intent currentLinkIntent() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return null;
        }
        Object saved = servletAttrs.getRequest().getAttribute(LinkingAuthorizationRequestRepository.REQUEST_ATTRIBUTE);
        if (saved instanceof OAuth2AuthorizationRequest authorization) {
            Object username = authorization.getAttributes().get(OAuthLinkSession.USERNAME);
            Object provider = authorization.getAttributes().get(OAuthLinkSession.PROVIDER);
            if (username != null && provider != null
                    && !username.toString().isBlank() && !provider.toString().isBlank()) {
                return new OAuthLinkSession.Intent(
                        username.toString(), provider.toString().toLowerCase(Locale.ROOT));
            }
        }
        return OAuthLinkSession.read(servletAttrs.getRequest(), linkSecret);
    }

    private void upgradeGeneratedUsername(User user, String preferred) {
        String current = user.getUsername();
        if (current == null || !current.startsWith("u_") || !current.substring(2).matches("[0-9a-fA-F]{32}")) {
            return;
        }
        String next = allocateUsername(preferred);
        if (!next.equals(current)) {
            user.changeUsername(next);
        }
    }

    private String preferredUsername(String provider, Map<?, ?> profile) {
        if ("github".equals(provider)) {
            return text(profile.get("login"));
        }
        if ("google".equals(provider)) {
            String email = text(profile.get("email"));
            if (email != null && email.contains("@")) {
                return email.substring(0, email.indexOf('@'));
            }
        }
        String nickname = text(profile.get("nickname"));
        if (nickname != null) return nickname;
        return text(profile.get("name"));
    }

    private String preferredNickname(Map<?, ?> profile, String fallbackUsername) {
        String nickname = text(profile.get("name"));
        if (nickname == null) nickname = text(profile.get("nickname"));
        if (nickname == null) nickname = text(profile.get("login"));
        if (nickname == null) nickname = fallbackUsername != null ? fallbackUsername : "새 회원";
        return nickname.substring(0, Math.min(50, nickname.length()));
    }

    private String allocateUsername(String preferred) {
        String base = sanitizeUsername(preferred);
        if (base == null) base = "user";
        String candidate = base;
        int attempt = 0;
        while (users.existsByUsername(candidate)) {
            attempt++;
            String suffix = "_" + attempt;
            int maxBase = Math.max(1, 50 - suffix.length());
            candidate = base.substring(0, Math.min(base.length(), maxBase)) + suffix;
            if (attempt > 1000) {
                return "u_" + UUID.randomUUID().toString().replace("-", "");
            }
        }
        return candidate;
    }

    private String sanitizeUsername(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "_")
                .replaceAll("^[._-]+", "")
                .replaceAll("[._-]+$", "");
        if (cleaned.isBlank()) return null;
        if (cleaned.length() > 50) cleaned = cleaned.substring(0, 50);
        if (cleaned.length() < 2) cleaned = cleaned + "01";
        return cleaned;
    }

    private String text(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }
}
