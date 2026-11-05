package com.newlecture.resuit.service.impl;

import com.newlecture.resuit.dto.BlogIntegrationRequest;
import com.newlecture.resuit.dto.BlogPostResponse;
import com.newlecture.resuit.dto.IntegrationResponse;
import com.newlecture.resuit.dto.PinnedReposRequest;
import com.newlecture.resuit.dto.RepoResponse;
import com.newlecture.resuit.entity.Integration;
import com.newlecture.resuit.entity.PinnedRepo;
import com.newlecture.resuit.entity.User;
import com.newlecture.resuit.repository.BlogPostRepository;
import com.newlecture.resuit.repository.IntegrationRepository;
import com.newlecture.resuit.repository.PinnedRepoRepository;
import com.newlecture.resuit.repository.UserRepository;
import com.newlecture.resuit.service.ProfileIntegrationService;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileIntegrationServiceImpl implements ProfileIntegrationService {
    private static final ParameterizedTypeReference<List<Map<String, Object>>> REPO_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final UserRepository users;
    private final IntegrationRepository integrations;
    private final BlogPostRepository blogPosts;
    private final PinnedRepoRepository pinnedRepos;
    private final RestClient githubApi = RestClient.builder()
            .baseUrl("https://api.github.com")
            .requestFactory(new JdkClientHttpRequestFactory())
            .build();

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationResponse> getIntegrations(UUID userId) {
        return integrations.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public IntegrationResponse connectBlog(UUID userId, BlogIntegrationRequest request) {
        if (request.platform() == null || request.blogId() == null) {
            throw badRequest("블로그 플랫폼과 아이디가 필요합니다.");
        }
        String platform = request.platform().toUpperCase(Locale.ROOT);
        String blogId = request.blogId().trim();
        if (!blogId.matches("[A-Za-z0-9-]{1,60}")) {
            throw badRequest("블로그 아이디 형식이 올바르지 않습니다.");
        }
        String rssUrl = switch (platform) {
            case "VELOG" -> "https://v2.velog.io/rss/" + blogId;
            case "TISTORY" -> "https://" + blogId + ".tistory.com/rss";
            default -> throw badRequest("지원하지 않는 블로그 플랫폼입니다.");
        };
        Integration integration = integrations.findByUserIdAndProvider(userId, "BLOG")
                .orElseGet(() -> integrations.save(Integration.builder()
                        .user(findUser(userId)).provider("BLOG").build()));
        integration.connectBlog(platform, blogId, rssUrl);
        return toResponse(integration);
    }

    @Override
    @Transactional
    public IntegrationResponse disconnect(UUID userId, String provider) {
        String normalized = provider.toUpperCase(Locale.ROOT);
        if (!normalized.equals("BLOG")) {
            throw badRequest("지원하지 않는 연동 종류입니다.");
        }
        Integration integration = integrations.findByUserIdAndProvider(userId, normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "연동 내역이 없습니다."));
        integration.disconnect();
        return toResponse(integration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPostResponse> getBlogPosts(String username, int size) {
        if (size < 1 || size > 20) throw badRequest("size는 1~20이어야 합니다.");
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return blogPosts.findByUserIdOrderByPublishedAtDesc(user.getId(), PageRequest.of(0, size))
                .stream().map(post -> new BlogPostResponse(post.getId(), post.getTitle(),
                        post.getDescription(), post.getUrl(), post.getPublishedAt())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepoResponse> getGithubRepos(String username, Boolean pinnedOnly) {
        if (Boolean.TRUE.equals(pinnedOnly)) {
            return getPinnedRepos(username);
        }
        return getPublicRepos(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepoResponse> getPinnedRepos(String username) {
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return pinnedRepos.findByUserIdOrderByOrderNoAsc(user.getId()).stream()
                .map(repo -> new RepoResponse(repo.getRepoFullName(),
                        "https://github.com/" + repo.getRepoFullName(), null, null, true))
                .toList();
    }

    @Override
    @Transactional
    public List<RepoResponse> setPinnedRepos(UUID userId, PinnedReposRequest request) {
        List<String> names = request.repoFullNames();
        if (names == null || names.size() > 3 || names.stream().distinct().count() != names.size()
                || names.stream().anyMatch(name -> name == null
                        || !name.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"))) {
            throw badRequest("대표 레포는 중복 없이 최대 3개까지 선택할 수 있습니다.");
        }
        pinnedRepos.deleteByUserId(userId);
        pinnedRepos.flush();
        User user = findUser(userId);
        for (int i = 0; i < names.size(); i++) {
            pinnedRepos.save(PinnedRepo.builder().user(user)
                    .repoFullName(names.get(i)).orderNo(i + 1).build());
        }
        return getPinnedRepos(user.getUsername());
    }

    private List<RepoResponse> getPublicRepos(String username) {
        User user = users.findByUsername(username).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Integration linked = integrations.findByUserIdAndProvider(user.getId(), "GITHUB")
                .filter(Integration::isConnected)
                .orElse(null);
        if (linked == null || linked.getAccessToken() == null || linked.getAccessToken().isBlank()
                || linked.getExternalId() == null || linked.getExternalId().isBlank()) {
            return List.of();
        }

        Set<String> pinnedNames = new HashSet<>(
                pinnedRepos.findByUserIdOrderByOrderNoAsc(user.getId()).stream()
                        .map(PinnedRepo::getRepoFullName)
                        .toList());

        List<Map<String, Object>> remote;
        try {
            remote = githubApi.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{login}/repos")
                            .queryParam("type", "owner")
                            .queryParam("sort", "updated")
                            .queryParam("per_page", 100)
                            .build(linked.getExternalId()))
                    .header("Authorization", "Bearer " + linked.getAccessToken())
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(REPO_LIST_TYPE);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "GitHub 공개 레포를 불러오지 못했습니다.");
        }

        if (remote == null || remote.isEmpty()) {
            return List.of();
        }

        return remote.stream()
                .filter(repo -> !Boolean.TRUE.equals(repo.get("private")))
                .map(repo -> {
                    String fullName = stringValue(repo.get("full_name"));
                    String url = stringValue(repo.get("html_url"));
                    if (url == null && fullName != null) {
                        url = "https://github.com/" + fullName;
                    }
                    return new RepoResponse(
                            fullName,
                            url,
                            stringValue(repo.get("description")),
                            stringValue(repo.get("language")),
                            fullName != null && pinnedNames.contains(fullName));
                })
                .filter(repo -> repo.fullName() != null && !repo.fullName().isBlank())
                .toList();
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private User findUser(UUID userId) {
        return users.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private IntegrationResponse toResponse(Integration integration) {
        return new IntegrationResponse(integration.getProvider(), integration.getPlatform(),
                integration.getExternalId(), integration.isConnected(), integration.getLastSyncedAt());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
