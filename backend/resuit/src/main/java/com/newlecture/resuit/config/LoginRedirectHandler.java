package com.newlecture.resuit.config;

import com.newlecture.resuit.service.GithubContributionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginRedirectHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    private final String frontend;
    private final String linkSecret;
    private final OAuth2AuthorizedClientService authorizedClients;
    private final GithubContributionService githubContributions;

    public LoginRedirectHandler(
            @Value("${resuit.auth.public-base-url:http://localhost:5173}") String base,
            @Value("${resuit.integration.token-secret:}") String linkSecret,
            OAuth2AuthorizedClientService authorizedClients,
            GithubContributionService githubContributions) {
        var uri = URI.create(base);
        if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("resuit.auth.public-base-url must be a public HTTP(S) application URL");
        }
        frontend = base.replaceAll("/+$", "");
        this.linkSecret = linkSecret;
        this.authorizedClients = authorizedClients;
        this.githubContributions = githubContributions;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        String linkedProvider = OAuthLinkSession.clear(request, response, linkSecret);
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
        if (linkedProvider != null) {
            response.sendRedirect(frontend + "/setting/integrations?linked="
                    + URLEncoder.encode(linkedProvider, StandardCharsets.UTF_8));
            return;
        }
        response.sendRedirect(frontend + "/");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String linkedProvider = OAuthLinkSession.clear(request, response, linkSecret);
        if (linkedProvider != null) {
            String code = "link_failed";
            if (exception instanceof OAuth2AuthenticationException oauth
                    && oauth.getError() != null
                    && oauth.getError().getErrorCode() != null) {
                code = oauth.getError().getErrorCode();
            }
            response.sendRedirect(frontend + "/setting/integrations?error="
                    + URLEncoder.encode(code, StandardCharsets.UTF_8));
            return;
        }
        response.sendRedirect(frontend + "/onboarding?error=oauth");
    }
}
