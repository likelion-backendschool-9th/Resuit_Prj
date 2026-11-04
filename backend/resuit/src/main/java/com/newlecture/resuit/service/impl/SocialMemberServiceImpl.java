package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.entity.SocialAccount;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.SocialAccountRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.SocialMemberService;

import jakarta.persistence.EntityManager;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialMemberServiceImpl implements SocialMemberService {
    private final UserRepository users;
    private final SocialAccountRepository accounts;
    private final EntityManager entityManager;

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
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtextextended(:key, 0))")
            .setParameter("key", provider + ":" + subject).getSingleResult();
        return accounts.findByProviderAndSubject(provider, subject).map(account -> account.getUser().getUsername())
            .orElseGet(() -> {
                String nickname = text(profile.get("name"));
                if (nickname == null) nickname = text(profile.get("nickname"));
                if (nickname == null) nickname = text(profile.get("login"));
                if (nickname == null) nickname = "새 회원";
                var user = users.save(User.builder().username("u_" + UUID.randomUUID().toString().replace("-", ""))
                    .nickname(nickname.substring(0, Math.min(50, nickname.length()))).role("USER").build());
                accounts.save(SocialAccount.builder().provider(provider).subject(subject).user(user).build());
                return user.getUsername();
            });
    }

    private String text(Object value) {
        return value == null || value.toString().isBlank() ? null : value.toString();
    }
}
