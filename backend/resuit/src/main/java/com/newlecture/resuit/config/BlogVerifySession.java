package com.newlecture.resuit.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

/** 블로그 소유 검증 대기 상태. ERD 없이 세션+쿠키만 사용한다. */
public final class BlogVerifySession {
    public static final String ATTR = "blogVerifyPending";
    public static final String COOKIE = "RESUIT_BLOG_VERIFY";
    public static final int MAX_AGE_SECONDS = 15 * 60;
    private static final String CODE_PREFIX = "resuit-verify-";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private BlogVerifySession() {}

    public static String newCode() {
        byte[] raw = new byte[5];
        RANDOM.nextBytes(raw);
        String suffix = B64.encodeToString(raw)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "x");
        if (suffix.length() < 8) {
            suffix = (suffix + "xxxxxxxx").substring(0, 8);
        } else {
            suffix = suffix.substring(0, 8);
        }
        return CODE_PREFIX + suffix;
    }

    public static void store(HttpServletRequest request, HttpServletResponse response,
            Pending pending, String secret) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR, pending);
        Cookie cookie = new Cookie(COOKIE, encode(pending, secret));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        if (request.isSecure()) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
    }

    public static Pending read(HttpServletRequest request, String secret) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object value = session.getAttribute(ATTR);
            if (value instanceof Pending pending && !pending.isExpired()) {
                return pending;
            }
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (COOKIE.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                Pending pending = decode(cookie.getValue(), secret);
                if (pending != null && !pending.isExpired()) {
                    return pending;
                }
            }
        }
        return null;
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ATTR);
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
    }

    private static String encode(Pending pending, String secret) {
        String payload = String.join(".",
                b64(pending.username()),
                b64(pending.platform()),
                b64(pending.blogId()),
                b64(pending.rssUrl()),
                b64(pending.code()),
                b64(Long.toString(pending.expiresAtEpochMs())));
        return payload + "." + sign(payload, secret);
    }

    private static Pending decode(String raw, String secret) {
        try {
            String[] parts = raw.split("\\.", 7);
            if (parts.length != 7) return null;
            String payload = parts[0] + "." + parts[1] + "." + parts[2] + "." + parts[3]
                    + "." + parts[4] + "." + parts[5];
            String expected = sign(payload, secret == null ? "" : secret);
            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    parts[6].getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            return new Pending(
                    ub64(parts[0]),
                    ub64(parts[1]),
                    ub64(parts[2]),
                    ub64(parts[3]),
                    ub64(parts[4]),
                    Long.parseLong(ub64(parts[5])));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String b64(String value) {
        return B64.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String ub64(String value) {
        return new String(B64D.decode(value), StandardCharsets.UTF_8);
    }

    private static String sign(String payload, String secret) {
        String material = (secret == null ? "" : secret) + "|" + payload;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return B64.encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public record Pending(
            String username,
            String platform,
            String blogId,
            String rssUrl,
            String code,
            long expiresAtEpochMs) {
        public boolean isExpired() {
            return Instant.now().toEpochMilli() > expiresAtEpochMs;
        }
    }
}
