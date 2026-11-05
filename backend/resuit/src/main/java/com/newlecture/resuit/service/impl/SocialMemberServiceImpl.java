package com.newlecture.resuit.service.impl;

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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialMemberServiceImpl implements SocialMemberService {
    private final UserRepository users;
    private final SocialAccountRepository accounts;
    private final EntityManager entityManager;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

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
