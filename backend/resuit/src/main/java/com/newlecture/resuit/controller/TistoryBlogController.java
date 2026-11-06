package com.newlecture.resuit.controller;

import com.newlecture.resuit.config.TistoryLinkSession;
import com.newlecture.resuit.dto.IntegrationResponse;
import com.newlecture.resuit.dto.TistoryBlogChoice;
import com.newlecture.resuit.dto.TistoryBlogSelectRequest;
import com.newlecture.resuit.service.CurrentUserResolver;
import com.newlecture.resuit.service.ProfileIntegrationService;
import com.newlecture.resuit.service.TistoryBlogClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TistoryBlogController {
    private final TistoryBlogClient tistory;
    private final ProfileIntegrationService integrations;
    private final CurrentUserResolver currentUser;
    private final String frontend;

    public TistoryBlogController(
            TistoryBlogClient tistory,
            ProfileIntegrationService integrations,
            CurrentUserResolver currentUser,
            @Value("${resuit.auth.public-base-url:http://localhost:5173}") String base) {
        this.tistory = tistory;
        this.integrations = integrations;
        this.currentUser = currentUser;
        this.frontend = base.replaceAll("/+$", "");
    }

    @GetMapping("/api/v1/users/me/integrations/tistory/start")
    public void start(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = currentUsername();
        if (username == null) {
            response.sendRedirect(frontend + "/onboarding");
            return;
        }
        if (!tistory.enabled()) {
            redirectError(response, "tistory_unconfigured");
            return;
        }
        String state = TistoryLinkSession.begin(request, username);
        response.sendRedirect(tistory.authorizationUrl(state));
    }

    @GetMapping("/api/login/tistory/callback")
    public void callback(
            String code,
            String state,
            String error,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        if (error != null && !error.isBlank()) {
            TistoryLinkSession.clear(request);
            redirectError(response, "tistory_denied");
            return;
        }
        String username = currentUsername();
        if (username == null || !TistoryLinkSession.matches(request, username, state) || code == null || code.isBlank()) {
            TistoryLinkSession.clear(request);
            redirectError(response, "tistory_state");
            return;
        }
        List<TistoryBlogChoice> blogs;
        try {
            String token = tistory.exchangeCode(code);
            blogs = tistory.listBlogs(token);
        } catch (RuntimeException ex) {
            TistoryLinkSession.clear(request);
            redirectError(response, "tistory_failed");
            return;
        }
        if (blogs.size() == 1) {
            try {
                integrations.connectTistory(currentUser.requireUserId(null), blogs.get(0).name());
            } catch (RuntimeException ex) {
                TistoryLinkSession.clear(request);
                redirectError(response, "tistory_rss");
                return;
            }
            TistoryLinkSession.clear(request);
            response.sendRedirect(frontend + "/setting/integrations?linked=tistory");
            return;
        }
        TistoryLinkSession.storeBlogs(request, username, blogs);
        response.sendRedirect(frontend + "/setting/integrations?tistory=choose");
    }

    @GetMapping("/api/v1/users/me/integrations/tistory/pending")
    public List<TistoryBlogChoice> pending(HttpServletRequest request) {
        String username = currentUsername();
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return TistoryLinkSession.pending(request, username);
    }

    @PostMapping("/api/v1/users/me/integrations/tistory")
    public IntegrationResponse select(
            @RequestBody TistoryBlogSelectRequest body,
            HttpServletRequest request) {
        String username = currentUsername();
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (body == null || body.name() == null || body.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "연결할 블로그를 선택해 주세요.");
        }
        boolean allowed = TistoryLinkSession.pending(request, username).stream()
                .anyMatch(blog -> body.name().equals(blog.name()));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "티스토리에서 확인된 블로그가 아닙니다. 다시 연결해 주세요.");
        }
        IntegrationResponse linked = integrations.connectTistory(currentUser.requireUserId(null), body.name());
        TistoryLinkSession.clear(request);
        return linked;
    }

    private void redirectError(HttpServletResponse response, String code) throws IOException {
        response.sendRedirect(frontend + "/setting/integrations?error="
                + URLEncoder.encode(code, StandardCharsets.UTF_8));
    }

    private static String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) return null;
        return name;
    }
}
