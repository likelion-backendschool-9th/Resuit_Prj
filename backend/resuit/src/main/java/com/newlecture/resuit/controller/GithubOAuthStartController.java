package com.newlecture.resuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 이전에 안내한 GitHub 로그인 주소. 로그아웃 페이지로 보내지 않고 인가 후 Resuit 콜백으로 돌아온다.
 */
@Controller
public class GithubOAuthStartController {
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;

    public GithubOAuthStartController(
            OAuth2AuthorizationRequestResolver authorizationRequestResolver,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository) {
        this.authorizationRequestResolver = authorizationRequestResolver;
        this.authorizationRequestRepository = authorizationRequestRepository;
    }

    @GetMapping("/api/oauth2/github/start")
    public void start(HttpServletRequest request, HttpServletResponse response) throws IOException {
        OAuth2AuthorizationRequest authorizationRequest =
                authorizationRequestResolver.resolve(request, "github");
        if (authorizationRequest == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "GitHub 로그인이 설정되지 않았습니다.");
            return;
        }
        authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        response.sendRedirect(authorizationRequest.getAuthorizationRequestUri());
    }
}
