package com.newlecture.resuit.config;

import com.newlecture.resuit.dto.TistoryBlogChoice;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/** 티스토리 블로그 연결 OAuth state와 고를 블로그 목록. 세션에만 둔다. */
public final class TistoryLinkSession {
    private static final String STATE = "tistoryLinkState";
    private static final String USERNAME = "tistoryLinkUsername";
    private static final String EXPIRES = "tistoryLinkExpires";
    private static final String BLOGS = "tistoryLinkBlogs";
    private static final int MAX_AGE_SECONDS = 10 * 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TistoryLinkSession() {}

    public static String begin(HttpServletRequest request, String username) {
        byte[] raw = new byte[24];
        RANDOM.nextBytes(raw);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        HttpSession session = request.getSession(true);
        session.setAttribute(STATE, state);
        session.setAttribute(USERNAME, username);
        session.setAttribute(EXPIRES, Instant.now().plusSeconds(MAX_AGE_SECONDS).toEpochMilli());
        session.removeAttribute(BLOGS);
        return state;
    }

    public static boolean matches(HttpServletRequest request, String username, String state) {
        HttpSession session = request.getSession(false);
        if (session == null || state == null || state.isBlank() || username == null) return false;
        Object saved = session.getAttribute(STATE);
        Object owner = session.getAttribute(USERNAME);
        Object expires = session.getAttribute(EXPIRES);
        if (!(saved instanceof String savedState) || !savedState.equals(state)) return false;
        if (!(owner instanceof String savedUser) || !savedUser.equals(username)) return false;
        if (!(expires instanceof Long epoch) || Instant.now().toEpochMilli() > epoch) return false;
        session.removeAttribute(STATE);
        return true;
    }

    public static void storeBlogs(HttpServletRequest request, String username, List<TistoryBlogChoice> blogs) {
        HttpSession session = request.getSession(true);
        session.setAttribute(USERNAME, username);
        session.setAttribute(EXPIRES, Instant.now().plusSeconds(MAX_AGE_SECONDS).toEpochMilli());
        session.setAttribute(BLOGS, List.copyOf(blogs));
    }

    public static List<TistoryBlogChoice> pending(HttpServletRequest request, String username) {
        HttpSession session = request.getSession(false);
        if (session == null || username == null) return List.of();
        if (!username.equals(session.getAttribute(USERNAME))) return List.of();
        Object expires = session.getAttribute(EXPIRES);
        if (!(expires instanceof Long epoch) || Instant.now().toEpochMilli() > epoch) return List.of();
        Object value = session.getAttribute(BLOGS);
        if (!(value instanceof List<?> list)) return List.of();
        return list.stream().filter(TistoryBlogChoice.class::isInstance).map(TistoryBlogChoice.class::cast).toList();
    }

    public static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return;
        session.removeAttribute(STATE);
        session.removeAttribute(USERNAME);
        session.removeAttribute(EXPIRES);
        session.removeAttribute(BLOGS);
    }
}
