package com.newlecture.resuit.service;

import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CurrentUserResolver {
    private final UserRepository users;
    private final boolean headerAuthEnabled;

    public CurrentUserResolver(UserRepository users,
            @Value("${app.dev-header-auth.enabled:false}") boolean headerAuthEnabled) {
        this.users = users;
        this.headerAuthEnabled = headerAuthEnabled;
    }

    public UUID requireUserId(String usernameHeader) {
        UUID fromSession = sessionUserId();
        if (fromSession != null) return fromSession;

        if (headerAuthEnabled && usernameHeader != null && !usernameHeader.isBlank()) {
            User user = users.findByUsername(usernameHeader).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
            return user.getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    public UUID optionalUserId(String usernameHeader) {
        UUID fromSession = sessionUserId();
        if (fromSession != null) return fromSession;

        if (!headerAuthEnabled || usernameHeader == null || usernameHeader.isBlank()) {
            return null;
        }
        return users.findByUsername(usernameHeader).map(User::getId).orElse(null);
    }

    private UUID sessionUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            return null;
        }
        return users.findByUsername(username).map(User::getId).orElse(null);
    }
}
