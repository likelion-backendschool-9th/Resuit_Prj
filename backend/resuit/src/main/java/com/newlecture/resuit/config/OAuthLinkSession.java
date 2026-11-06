package com.newlecture.resuit.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

/**
 * 로그인 후 계정 연결 OAuth용 상태.
 * OAuth 리다이렉트에서 세션이 바뀌어도 유지되도록 HttpOnly 쿠키에도 둔다.
 */
public final class OAuthLinkSession {
    public static final String PROVIDER = "oauthLinkProvider";
    public static final String USERNAME = "oauthLinkUsername";
    public static final String COOKIE = "RESUIT_OAUTH_LINK";
    private static final int MAX_AGE_SECONDS = 600;

    private OAuthLinkSession() {}

    public static String issue(String username, String provider, String secret) {
        return encode(username, provider.trim().toLowerCase(Locale.ROOT), secret);
    }

    /** 인가 시작 요청의 link 토큰을 OAuth state와 같은 세션에 붙인다. */
    public static void captureRequestToken(HttpServletRequest request, String secret) {
        String token = request.getParameter("link");
        if (token == null || token.isBlank()) return;
        Intent intent = decode(token, secret);
        if (intent == null) return;
        HttpSession session = request.getSession(true);
        session.setAttribute(PROVIDER, intent.provider());
        session.setAttribute(USERNAME, intent.username());
    }

    public static void store(HttpServletRequest request, HttpServletResponse response,
            String username, String provider, String secret) {
        String normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
        HttpSession session = request.getSession(true);
        session.setAttribute(PROVIDER, normalizedProvider);
        session.setAttribute(USERNAME, username);
        Cookie cookie = new Cookie(COOKIE, encode(username, normalizedProvider, secret));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        if (request.isSecure()) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
    }

    public static Intent parse(String token, String secret) {
        if (token == null || token.isBlank()) return null;
        return decode(token, secret);
    }

    public static Intent read(HttpServletRequest request, String secret) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute(USERNAME);
            Object provider = session.getAttribute(PROVIDER);
            if (username != null && provider != null
                    && !username.toString().isBlank() && !provider.toString().isBlank()) {
                return new Intent(username.toString(), provider.toString().toLowerCase(Locale.ROOT));
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return decode(cookie.getValue(), secret);
            }
        }
        return null;
    }

    /** @return 연결 중이던 provider id (없으면 null) */
    public static String clear(HttpServletRequest request, HttpServletResponse response, String secret) {
        String provider = null;
        Object saved = request.getAttribute(LinkingAuthorizationRequestRepository.REQUEST_ATTRIBUTE);
        if (saved instanceof org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest authorization) {
            Object value = authorization.getAttributes().get(PROVIDER);
            if (value != null && !value.toString().isBlank()) {
                provider = value.toString();
            }
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object value = session.getAttribute(PROVIDER);
            if (value != null && !value.toString().isBlank()) {
                provider = value.toString();
            }
            session.removeAttribute(PROVIDER);
            session.removeAttribute(USERNAME);
        }
        if (provider == null) {
            Intent intent = readCookieOnly(request, secret);
            if (intent != null) provider = intent.provider();
        }
        Cookie cookie = new Cookie(COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        if (request.isSecure()) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
        return provider;
    }

    private static Intent readCookieOnly(HttpServletRequest request, String secret) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return decode(cookie.getValue(), secret);
            }
        }
        return null;
    }

    private static String encode(String username, String provider, String secret) {
        long expiresAt = System.currentTimeMillis() + MAX_AGE_SECONDS * 1000L;
        String payload = username + "|" + provider + "|" + expiresAt;
        String signature = sign(payload, secret);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    private static Intent decode(String raw, String secret) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(raw), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 4);
            if (parts.length != 4) return null;
            String username = parts[0];
            String provider = parts[1];
            long expiresAt = Long.parseLong(parts[2]);
            String signature = parts[3];
            if (expiresAt < System.currentTimeMillis()) return null;
            String payload = username + "|" + provider + "|" + expiresAt;
            String expected = sign(payload, secret == null ? "" : secret);
            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            if (username.isBlank() || provider.isBlank()) return null;
            return new Intent(username, provider.toLowerCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String sign(String payload, String secret) {
        String material = (secret == null ? "" : secret) + "|" + payload;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record Intent(String username, String provider) {}
}
