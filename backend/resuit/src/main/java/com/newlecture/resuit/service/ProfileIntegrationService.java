package com.newlecture.resuit.service;

import com.newlecture.resuit.dto.BlogIntegrationRequest;
import com.newlecture.resuit.dto.BlogPostResponse;
import com.newlecture.resuit.dto.BlogVerifyStartResponse;
import com.newlecture.resuit.dto.IntegrationResponse;
import com.newlecture.resuit.dto.PinnedReposRequest;
import com.newlecture.resuit.dto.RepoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

public interface ProfileIntegrationService {
    List<IntegrationResponse> getIntegrations(UUID userId);
    IntegrationResponse connectBlog(UUID userId, BlogIntegrationRequest request);
    BlogVerifyStartResponse startBlogVerify(UUID userId, BlogIntegrationRequest request,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    IntegrationResponse confirmBlogVerify(UUID userId,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    IntegrationResponse connectTistory(UUID userId, String blogName);
    IntegrationResponse syncGithub(UUID userId);
    IntegrationResponse syncBlog(UUID userId);
    IntegrationResponse disconnect(UUID userId, String provider);
    List<BlogPostResponse> getBlogPosts(String username, int size);
    List<RepoResponse> getGithubRepos(String username, Boolean pinnedOnly);
    List<RepoResponse> getPinnedRepos(String username);
    List<RepoResponse> setPinnedRepos(UUID userId, PinnedReposRequest request);
}
