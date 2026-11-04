package com.newlecture.resuit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginRedirectHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private final String frontend;

    public LoginRedirectHandler(@Value("${resuit.auth.public-base-url:http://localhost:5174}") String base) {
        var uri = URI.create(base);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("resuit.auth.public-base-url must be a public HTTP(S) application URL");
        }
        frontend = base.replaceAll("/+$", "");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        response.sendRedirect(frontend + "/");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        response.sendRedirect(frontend + "/onboarding?error=oauth");
    }
}
