package com.newlecture.resuit.config;

import com.newlecture.resuit.service.GithubContributionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginRedirectHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private final String frontend;
    private final OAuth2AuthorizedClientService authorizedClients;
    private final GithubContributionService githubContributions;

    public LoginRedirectHandler(
            @Value("${resuit.auth.public-base-url:http://localhost:5174}") String base,
            OAuth2AuthorizedClientService authorizedClients,
            GithubContributionService githubContributions) {
        var uri = URI.create(base);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("resuit.auth.public-base-url must be a public HTTP(S) application URL");
        }
        frontend = base.replaceAll("/+$", "");
        this.authorizedClients = authorizedClients;
        this.githubContributions = githubContributions;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken oauth
                && "github".equals(oauth.getAuthorizedClientRegistrationId())) {
            OAuth2AuthorizedClient client = authorizedClients.loadAuthorizedClient(
                    oauth.getAuthorizedClientRegistrationId(), oauth.getName());
            OAuth2User principal = oauth.getPrincipal();
            Object login = principal.getAttributes().get("login");
            if (client != null && client.getAccessToken() != null && login != null) {
                githubContributions.linkGithubAccount(
                        oauth.getName(),
                        login.toString(),
                        client.getAccessToken().getTokenValue());
            }
        }
        response.sendRedirect(frontend + "/");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        response.sendRedirect(frontend + "/onboarding?error=oauth");
    }
}
